def check_syntax(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        text = f.read()
    
    backticks = text.count('`')
    print('Backticks count:', backticks)
    if backticks % 2 != 0:
        print('UNBALANCED BACKTICKS!')
    
    braces = text.count('{') - text.count('}')
    print('Braces delta (should be 0):', braces)
    
    parens = text.count('(') - text.count(')')
    print('Parens delta (should be 0):', parens)

check_syntax('src/main/resources/static/app.js')
