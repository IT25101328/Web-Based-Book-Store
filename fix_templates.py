import glob

for filepath in glob.glob('src/main/resources/static/*.html') + glob.glob('src/main/resources/static/*.js'):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Fix broken template literals
    content = content.replace('Rs Rs {', 'Rs ${')
    content = content.replace('Rs {', '${')
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
print('Fixed template literals')
