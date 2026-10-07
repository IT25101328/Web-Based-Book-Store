import os
import re

static_dir = r"c:\Users\Nadun\Documents\BookStore\bookstore\src\main\resources\static"

# Two types of injections:
# 1. List-based (catalog.html, book-details.html, support.html)
# Look for: <li class="auth-only" style="display:none;"><a href="cart.html"
# Or: <li><a href="cart.html"
list_injection = """
        <li class="auth-only dropdown" style="display:none;">
          <a href="#" class="nav-link" onclick="toggleNotificationDropdown(event)">
            🔔 <span id="notificationBadge" class="badge" style="display:none; background: #d4a373; color: black; border-radius: 50%; padding: 2px 6px; font-size: 12px; margin-left: 4px;">0</span>
          </a>
          <div class="dropdown-content notification-dropdown" id="notificationDropdownContent" style="width: 320px; padding: 1rem; right: 0; left: auto; display:none; position:absolute; background: rgba(20,20,20,0.95); border: 1px solid var(--gold); border-radius: 8px; z-index: 999;">
            <!-- filled by JS -->
          </div>
        </li>
"""

# 2. Div-based (index.html, wishlist.html)
# Look for: <!-- Cart --> or <div class="cart-icon
div_injection = """
        <div class="auth-only dropdown" style="display:none; position:relative;">
          <button class="btn-primary" style="padding: 0.5rem 1rem;" onclick="toggleNotificationDropdown(event)">
            🔔 <span id="notificationBadge" class="badge" style="display:none; background: #d4a373; color: black; border-radius: 50%; padding: 2px 6px; font-size: 12px; margin-left: 4px;">0</span>
          </button>
          <div class="dropdown-content notification-dropdown" id="notificationDropdownContent" style="width: 320px; padding: 1rem; right: 0; left: auto; display:none; position:absolute; background: rgba(20,20,20,0.95); border: 1px solid var(--gold); border-radius: 8px; z-index: 999; margin-top: 0.5rem;">
            <!-- filled by JS -->
          </div>
        </div>
"""

for filename in os.listdir(static_dir):
    if not filename.endswith(".html"):
        continue
    
    filepath = os.path.join(static_dir, filename)
    with open(filepath, "r", encoding="utf-8") as f:
        content = f.read()

    # Skip if already injected
    if "notificationBadge" in content:
        continue

    new_content = content
    modified = False

    # Try to match the list-based injection point:
    # <li><a href="cart.html" or <li class="auth-only"...><a href="cart.html"
    if re.search(r'(<li[^>]*>\s*<a[^>]*href="cart\.html")', content):
        new_content = re.sub(r'(<li[^>]*>\s*<a[^>]*href="cart\.html")', list_injection + r'\1', content)
        modified = True
    # Else try to match div-based for index.html (<!-- Cart -->)
    elif "<!-- Cart -->" in content:
        new_content = content.replace("<!-- Cart -->", div_injection + "\n        <!-- Cart -->")
        modified = True
    # Else try wishlist (div class="cart-icon")
    elif '<div class="cart-icon no-admin auth-only"' in content:
        new_content = content.replace('<div class="cart-icon no-admin auth-only"', div_injection + '\n        <div class="cart-icon no-admin auth-only"')
        modified = True

    if modified:
        with open(filepath, "w", encoding="utf-8") as f:
            f.write(new_content)
        print(f"Injected into {filename}")
