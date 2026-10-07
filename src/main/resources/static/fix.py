import re

def fix_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Use regex to replace the content of <span id="themeIcon">
    content = re.sub(r'<span id="themeIcon">[^<]+</span>', '<span id="themeIcon">☀️</span>', content)
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

fix_file('finance-dashboard.html')
fix_file('admin-dashboard.html')
print("Done")
