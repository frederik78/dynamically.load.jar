# Chargement de plugins dynamiquement

Cette application charge de manière dynamique des archives ***.jar** ajoutée au répertoire $HOME

Le projet se découpe en deux blocs.  
Le premier projet intitulé **dynamically.load.jar** écoute le répertoire $HOME et scrute l'ajout de nouveaux fichiers.
Lorsqu'une archive ***.jar** est détectée une implémentation de l'interface **fr.frederic.dynamically.load.jar.plugin.PluginAction** est recherchée.  
Ensuite le plugin chargé dans le classloader et est exécuté.

Le projet **hello.plugin** est un plugin destiné à être exécuté par le projet **dynamically.load.jar**
Pour cela il suffit de générer une archive du projet au moyen de la commande   
**mvn clean install**.   
Ensuite il ne reste plus qu'à recopier cette archive nouvellement générée dans le répertoire $HOME 
(Veiller à ce que le projet **dynamically.load.jar** soit en cours d'exécution)