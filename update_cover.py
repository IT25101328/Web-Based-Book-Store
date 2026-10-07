import re
import glob

def replacer(match):
    prefix = match.group(1)
    return '${' + prefix + '.coverImage ? `<img src="/uploads/covers/${' + prefix + '.coverImage}" style="width:100%; height:100%; object-fit:cover; border-radius:inherit;"/>` : \'📖\'}'

# Process app.js
with open('src/main/resources/static/app.js', 'r', encoding='utf-8') as f:
    content = f.read()
    
# Replace standard ${b.coverEmoji || 'xxx'}
content = re.sub(r'\$\{([a-zA-Z0-9_\.]+)\.coverEmoji\s*\|\|\s*\'[^\']+\'\}', replacer, content)

# Replace detailCoverEmoji textContent to innerHTML
content = re.sub(
    r"document\.getElementById\('detailCoverEmoji'\)\.textContent = ([a-zA-Z0-9_\.]+)\.coverEmoji \|\| '[^']+';",
    r"document.getElementById('detailCoverEmoji').innerHTML = \1.coverImage ? `<img src='/uploads/covers/${\1.coverImage}' style='width:100%; height:100%; object-fit:cover; border-radius:inherit;'/>` : '📖';",
    content
)

# Remove coverEmoji from book payloads in app.js
content = re.sub(r'coverEmoji: document\.getElementById\([^)]+\)\.value \|\| \'[^\']+\',?\s*', '', content)
content = re.sub(r'document\.getElementById\([^)]+\)\.value = book\.coverEmoji \|\| \'[^\']+\';\s*', '', content)

with open('src/main/resources/static/app.js', 'w', encoding='utf-8') as f:
    f.write(content)

# Process all html files
for html_file in glob.glob('src/main/resources/static/*.html'):
    if html_file.endswith('admin-add-book.html'):
        continue
    with open(html_file, 'r', encoding='utf-8') as f:
        html_content = f.read()
        
    html_content = re.sub(r'\$\{([a-zA-Z0-9_\.]+)\.coverEmoji\s*\|\|\s*\'[^\']+\'\}', replacer, html_content)
    
    with open(html_file, 'w', encoding='utf-8') as f:
        f.write(html_content)

print("Updated app.js and HTML files.")
