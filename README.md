{
"models": [
{
"model": "User",
"properties": [
{
"name": "id",
"type": "Long",
"primaryKey": true
},
{
"name": "name",
"type": "String"
},
{
"name": "email",
"type": "String"
}
],
"repository": true,
"service": true,
"controller": [
    path: "/account"
    method: get
    description: "este metodo necesito que haga bla bla"
    response: "id: 1"
]
"test": true
},
{
"model": "Product",
"properties": [
{
"name": "id",
"type": "Long",
"primaryKey": true
},
{
"name": "name",
"type": "String"
},
{
"name": "price",
"type": "Double"
}
],
"repository": true,
"service": true,
"controller": true,
"test": true
}
]
}