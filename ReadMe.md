# Bienvenu dans le framework Wavie

Les etapes à suivres sont:

Sprint0:
- Creez votre fichier web.xml
- Declarer servlet: FrontController(classe: **mg.itu.prom16.FrontController**) avec l'url : "/" dans web.xml

Sprint1:
- initialiser parametre package des controller dans web.xml:
    - nom: package-controller
    - nom du servlet sur lequel l'associer: FrontController
- Annotez vos controller avec l'annotation @controller

Sprint2:
- Declarez le nom de votre projet en tant que paramettre dans le fichier web.xml
  - nom: project-name
  - nom du servlet sur lequel l'associer: FrontController
