A*-Algorithmus
==============
###### Roland Bernard - 4InfB

## Theoretischer Hintergrund

Der A*-Algorithmus ist ein Algorithmus um den kürzesten Pfad zwischen zwei
Knoten in einem Graphen mit positiven Kantengewichtungen zu finden. Der
Algorithmus ist eine Erweiterung des Dijkstra-Algorithmus. Anders als der
Dijkstra-Algorithmus nutzt der A*-Algorithmus eine Schätzung um zielgerichteter
zu suchen. Der Algorithmus funktioniert folgendermaßen:
 1. Es werden zwei listen genutzt. Die offene und die geschlossene Liste. Anfangs
   ist die geschlossene Liste leer und die offene Liste enthält einen der Knoten,
   zwischen denen der Pfad errechnet werden soll.
 2. Der erste Knoten in der offenen Liste wird aufgelöst. Dazu werden alle Knoten,
   mit denen der aufzulösende Konten verbunden ist, in die offene liste gegeben.
   Es wird auch festgehalten, woher man zum Knoten kam.
 3. Der erste Knoten wird nun von der offenen in die geschlossene Liste gegeben.
 4. Die offene Liste wird nun nach bisherigen Kosten+geschätzten Knoten geordnet
   und wenn der erste Knoten nicht der zu erreichende Knoten ist, wird zu 2.
   gesprungen. Anderenfalls wurde der beste Pfad gefunden.
Solange der A*-Algorithmus immer nur die noch benötigte Zeit unterschätzt wird
der gefundene Pfad auch optimal sein. Das liegt daran, dass wenn der Zielknoten
der mit den geringsten Kosten in der offenen Liste ist, es nicht mehr mäglich ist,
dorthin schneller zu gelangen, weil die anderen ja unterschätzt sind.

## Benutzerhandbuch

Der Graph kann über ein eigenes Format aus einer Datei eingelesen werden. Dabei
können Knoten hinzugefügt werden: `n <Name>:<X-Position>;<Y-Position>` Bsp.:
`n knoten1:0.25;1.5` Es ist auch möglich keinen Namen anzugeben: `n :2.5;0.5` 
Dann ist es auch möglich Verbindungen hinzuzufügen mit 
`c <Knoten> =<Gewichtung>= <Knoten>` Bsp.: `c knoten1 =2= knoten2`
Es ist auch möglich die Richtung der Verbindung festzulegen: `c knoten1 ==> knoten2`
Wird keine Gewichtung angegeben wird diese durch die distanz zwischen den Knoten
errechnet. Die Knoten können entweder mit Namen oder mit allen Daten (Name+Position)
angegeben werden, falls kein Name Vorhanden ist.
Bsp.:
```
n start: 0;0
n ende: 1;1
n : 1.5;0
n : 0;1.5
c start == : 1.5;0
c :1.5;0 =0.5=> : 0;1.5
c ende <== : 0;1.5
c ende <== : 1.5;0
```
Es kann auch die GUI genutzt werden, um einen Graphen zu erstellen.
Ein Graph kann dann mit `Load` geladen werden und im Tab `Calculate` können zwei
Knoten ausgewählt werden und der kürzeste Pfad dazwischen errechnet werden.

## Implementierung

Der Algorithmus selbst ist in `AStern` implementiert und eine GUI um die Nutzung
zu vereinfachen ist in `gui.ASternGui`,`gui.ASternWindow`,`gui.ASternPlayer` und
`gui.ASternEditor` implementiert worden.

## Tests

Die JUnit-Tests sind in `ASternTest` enthalten.

