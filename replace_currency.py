import glob
import re

for filepath in glob.glob('src/main/resources/static/*.html') + glob.glob('src/main/resources/static/*.js'):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Replace instances like Price ($) or \$[0-9] or \${Number
    content = content.replace('Price ($)', 'Price (Rs)')
    content = content.replace('Price ($) *', 'Price (Rs) *')
    
    # We want to replace >$ with >Rs or > $ with > Rs
    content = re.sub(r'>\$(\$?[0-9{])', r'>Rs \1', content)
    content = re.sub(r'\$(\{Number)', r'Rs \1', content)
    
    # specific replacements in app.js for textContent and alert/toast
    content = content.replace("'$' +", "'Rs ' +")
    content = content.replace(">$${Number", ">Rs ${Number")
    content = content.replace(">$${Number", ">Rs ${Number")
    content = content.replace("$$${Number", "$Rs ${Number") # wait, template literal might be `$${...}`
    content = content.replace(">$$", ">Rs $") # to fix `>$${` -> `>Rs ${`
    
    # e.g. <td style="font-weight:bold; color:var(--primary);">$${Number(b.price).toFixed(2)}</td>
    content = content.replace(">$${", ">Rs ${")
    content = content.replace("<td>$${", "<td>Rs ${")
    content = content.replace("$$${", "Rs ${")

    # In cart page: <h3 id="cartTotalText">$0.00</h3>
    content = content.replace('>$0.00<', '>Rs 0.00<')
    
    # In checkout summary: <span>$${cartTotal.toFixed(2)}</span>
    content = content.replace(">$${cartTotal", ">Rs ${cartTotal")
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
print('Replaced $ with Rs')
