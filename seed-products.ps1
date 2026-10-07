# ============================================================
# NexaCart - 100 Product Seeder
# Prerequisite:
#   1. Dockerized NexaCart backend is running on :8080
#   2. An ADMIN JWT is stored in $token
# ============================================================

if (-not $token) {
    throw "JWT token not found. Login as an ADMIN first and store the JWT in `$token."
}

# ------------------------------------------------------------
# Product names: 10 categories × 10 products = 100 products
# ------------------------------------------------------------

$catalog = @{
    "Electronics" = @(
        "Nova X2 Smartphone",
        "AeroBook Pro 15 Laptop",
        "Pulse Wireless Earbuds",
        "Orbit Pro Smartwatch",
        "PixelView 32 Monitor",
        "Volt Mechanical Keyboard",
        "StreamPro 4K Webcam",
        "Echo Max Bluetooth Speaker",
        "ChargeHub USB-C Station",
        "AirLink Mesh Router"
    )

    "Fashion" = @(
        "Urban Classic Hoodie",
        "StreetFlex Sneakers",
        "Premium Denim Jacket",
        "Metro Travel Backpack",
        "Oxford Casual Shirt",
        "Slim Fit Chinos",
        "Essential Cotton Tee",
        "Aero Training Jacket",
        "Classic Minimal Watch",
        "Canvas Crossbody Bag"
    )

    "Home & Kitchen" = @(
        "Nordic Floor Lamp",
        "Cloud Comfort Cushion Set",
        "BrewMaster Coffee Maker",
        "CrispAir Digital Fryer",
        "PureSteel Cookware Set",
        "SmartBlend Mixer",
        "SoftWeave Bedsheet Set",
        "Ceramic Dining Set",
        "Bamboo Storage Organizer",
        "ThermoSteel Water Bottle"
    )

    "Books" = @(
        "The Pragmatic Programmer",
        "Designing Data-Intensive Applications",
        "Sapiens",
        "Thinking Fast and Slow",
        "Ikigai",
        "The Psychology of Money",
        "Zero to One",
        "Rich Dad Poor Dad",
        "The Lean Startup",
        "Algorithms Unlocked"
    )

    "Sports & Fitness" = @(
        "FlexGrip Yoga Mat",
        "Sprint Pro Running Shoes",
        "Trail Hydration Bottle",
        "FitBand Resistance Kit",
        "PowerLift Dumbbell Pair",
        "CoreFlex Training Gloves",
        "SpeedJump Skipping Rope",
        "Recover Foam Roller",
        "Ace Tennis Racket",
        "CourtPro Basketball"
    )

    "Beauty" = @(
        "Glow Daily Skincare Kit",
        "Velvet Eau de Parfum",
        "Hydrate Face Serum",
        "Silk Repair Shampoo",
        "CalmClean Face Wash",
        "Luma Moisture Cream",
        "PureTone Lip Balm Set",
        "Aura Hair Dryer",
        "SoftGlow Body Lotion",
        "Vivid Makeup Brush Set"
    )

    "Gaming" = @(
        "Vertex Gaming Mouse",
        "HyperStrike Gamepad",
        "Nova RGB Gaming Keyboard",
        "Arena XL Mouse Pad",
        "Pulse Gaming Headset",
        "GameVault External SSD",
        "ProAim Controller Stand",
        "QuestLink USB Cable",
        "StreamDeck Mini",
        "Arcade LED Light Bar"
    )

    "Accessories" = @(
        "Everyday Leather Wallet",
        "Urban Polarized Sunglasses",
        "Travel Cable Organizer",
        "Classic Canvas Belt",
        "Metro Key Holder",
        "FoldLite Umbrella",
        "Aero Passport Holder",
        "Minimal Card Case",
        "Daily Utility Pouch",
        "Urban Travel Neck Pillow"
    )

    "Grocery" = @(
        "Morning Roast Coffee",
        "GreenLeaf Organic Tea",
        "Harvest Trail Granola",
        "GoldenFields Honey",
        "Sunrise Peanut Butter",
        "FreshMill Oats",
        "Kitchen Select Olive Oil",
        "NutriMix Trail Nuts",
        "FarmBasket Brown Rice",
        "DailyHarvest Pasta"
    )

    "Office & Study" = @(
        "FocusDesk Notebook",
        "WritePro Gel Pen Set",
        "ErgoStudy Desk Lamp",
        "ClearView Desk Organizer",
        "StudyMate Whiteboard",
        "QuietType Keyboard",
        "Precision Scientific Calculator",
        "CloudRest Laptop Stand",
        "ColorNote Marker Set",
        "WorkFlow File Folder Set"
    )
}

# ------------------------------------------------------------
# Base prices for each category
# ------------------------------------------------------------

$basePrices = @{
    "Electronics"      = 2999
    "Fashion"          = 799
    "Home & Kitchen"   = 999
    "Books"            = 399
    "Sports & Fitness" = 699
    "Beauty"           = 499
    "Gaming"           = 799
    "Accessories"      = 499
    "Grocery"          = 199
    "Office & Study"   = 299
}

# ------------------------------------------------------------
# Read existing products
# ------------------------------------------------------------

$current = Invoke-RestMethod `
    "http://localhost:8080/products?page=0&size=100" `
    -Headers @{ Authorization = "Bearer $token" }

$currentNames = @(
    $current.content | ForEach-Object { $_.name }
)

$currentCount = [int]$current.totalElements

Write-Host ""
Write-Host "Current products : $currentCount"
Write-Host "Target products  : 100"
Write-Host ""

# ------------------------------------------------------------
# Stop if already at 100+
# ------------------------------------------------------------

if ($currentCount -ge 100) {
    Write-Host "Catalog already contains $currentCount products."
    Write-Host "Nothing to add."
    exit 0
}

# ------------------------------------------------------------
# Build list of missing products
# ------------------------------------------------------------

$productsToCreate = @()
$productNumber = 1

foreach ($category in $catalog.Keys) {

    $names = $catalog[$category]
    $basePrice = $basePrices[$category]

    for ($i = 0; $i -lt $names.Count; $i++) {

        $name = $names[$i]

        # Skip if this product already exists
        if ($currentNames -contains $name) {
            $productNumber++
            continue
        }

        # Generate a realistic varying price
        $price = $basePrice + (($i + 1) * 500) + (($productNumber % 4) * 199)

        # Generate varying stock
        $stock = 10 + (($productNumber * 7) % 45)

        # Safe URL slug
        $slug = $name.ToLower()
        $slug = $slug -replace '[^a-z0-9]+', '-'
        $slug = $slug.Trim('-')

        $product = @{
            name = $name
            description = "$name is a premium $category product designed for everyday use, reliability and great value."
            price = $price
            imageUrl = "https://picsum.photos/seed/nexacart-$slug/800/800"
            category = $category
            stockQuantity = $stock
        }

        $productsToCreate += $product

        $productNumber++
    }
}

# ------------------------------------------------------------
# Only create enough products to reach exactly 100
# ------------------------------------------------------------

$needed = 100 - $currentCount

$productsToCreate = @(
    $productsToCreate | Select-Object -First $needed
)

Write-Host "Products to create: $($productsToCreate.Count)"
Write-Host ""

# ------------------------------------------------------------
# Create products through the real NexaCart API
# ------------------------------------------------------------

$created = 0
$failed = 0

foreach ($product in $productsToCreate) {

    try {

        Invoke-RestMethod `
            "http://localhost:8080/products" `
            -Method POST `
            -Headers @{ Authorization = "Bearer $token" } `
            -ContentType "application/json" `
            -Body ($product | ConvertTo-Json)

        $created++

        Write-Host "Created: $($product.name)"
    }
    catch {

        $failed++

        Write-Host "FAILED: $($product.name)"
        Write-Host "Reason: $($_.Exception.Message)"
    }
}

# ------------------------------------------------------------
# Final result
# ------------------------------------------------------------

Write-Host ""
Write-Host "========================================"
Write-Host "NexaCart Product Seeding Complete"
Write-Host "========================================"
Write-Host "Started with : $currentCount"
Write-Host "Created      : $created"
Write-Host "Failed       : $failed"
Write-Host "Target       : 100"
Write-Host "========================================"
Write-Host ""

# ------------------------------------------------------------
# Verify final count
# ------------------------------------------------------------

$final = Invoke-RestMethod `
    "http://localhost:8080/products?page=0&size=100" `
    -Headers @{ Authorization = "Bearer $token" }

Write-Host "Final product count: $($final.totalElements)"