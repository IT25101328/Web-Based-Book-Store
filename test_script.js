const list = [{id: 1, title: 'Test Book Updated', author: 'Test Author', price: 10, stock: 5, genre: ''}];
const html = list.map(function(b){return '<button onclick="openEdit(' + JSON.stringify(b).replace(/\"/g, '&quot;') + ')">Edit</button>';}).join('');
console.log(html);
