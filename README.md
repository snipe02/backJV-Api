
## testar se estar fucionando
https://backjv-api.onrender.com//api/technologies



___________________________________________________________
## 1 ) O que ele faz é cadastrar um novo feedback para o projeto com ID = 1.
POST http://localhost:3000/api/projects/1/feedbacks
{
  "nota": 5,
  "comentario": "meu projeto!",
  "author": "Luk",
  "message": "Gostei muito da implementação."
}
___________________________________________________________________

_______________________________________________________________
## Serve para listar os projetos cadastrados no sistema
1) GET http://localhost:3000/api/projects
_______________________________________________________________
## Listar todos os projetos (sem filtro):
http://localhost:3000/api/projects?page=0&size=5
____________________________________________________________
## Listar projetos filtrando por tecnologia:
http://localhost:3000/api/projects?technology=Java&page=0&size=10
____________________________________________________________
## 
