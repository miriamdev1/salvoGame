
$(document).ready(function(){
      $("#logout-btn").click(function () {
        logout();
      });
      $("#returnToGames").click(function () {
          backToGame();
        });
});


function logout() {
	$.post("/api/logout")
		.done(function () {
			console.log("logged out");
			//location.replace("/web/games.html");
			location.href='games.html'
		})
};
function getParameterByName(name) {
	var match = RegExp('[?&]' + name + '=([^&]*)').exec(window.location.search);
	return match && decodeURIComponent(match[1].replace(/\+/g, ' '));
};

function backToGame() {
	window.location = 'web/games.html';
}

var app = new Vue({
    el: '#app',

    data: {
        gameView: [],
        currentPlayer:"",
        currentPlayerId: "",
        opponent:"",
        ships: [],
        salvoes:[],
        ship_count:"",
        hit:"",
        cell:""
     }
})

getGameView()

function getGameView(){
    fetch("/api/game_view/"+ getParameterByName('gp'))
    .then(function(response) {
        return response.json()
    })
    .then(function(json) {
            gameView = json;
            console.log(gameView);
            app.currentPlayer = gameView.gamePlayers[0].player.email;
            gamePlayerId = getParameterByName('gp');
            app.ships = json.ships;
            placeSalvoes();

            if (gameView.gamePlayers.length == 2) {
                       	   if (gameView.gamePlayers[0].gpId == getParameterByName('gp')){
                    				app.currentPlayer = gameView.gamePlayers[0].player.email;
                    				app.currentPlayerId =gameView.gamePlayers[0].player.playerId;
                      				app.opponent = gameView.gamePlayers[1].player.email;
                   			} else {
                        		    app.currentPlayer =  gameView.gamePlayers[1].player.email;
                        		    app.currentPlayerId =gameView.gamePlayers[1].player.playerId;
                        		    app.opponent = gameView.gamePlayers[0].player.email;
                            		}
                        }
                        if (gameView.gamePlayers.length == 1){
                        			 app.currentPlayer = gameView.gamePlayers[0].player.email;
                        			 app.opponent = " Waiting for player...";
                        			 if (app.opponent = " Waiting for player..."){
                                                 	$("#salvoes-grid").show();
                                                 	$(".grid-salvoes").show();
                                     }

                        }


             if(gameView.ships.length == 0  && gameView.salvoes == 0){
                                        loadGrid(false)
                                         $("#salvoes").hide();
                                         $("#salvoes-grid").hide();
                                         $("#add-salvoes").hide();
                                         $(".grid-salvoes").hide();
                                         $("#gameHistory").hide();
                                        // disableCellsalvo();
             }
             if(gameView.ships.length > 0 && gameView.salvoes.length ==0) {
                                        loadGrid(true)
                                        $("#add-salvoes").show();
                                        $("#gameHistory").hide();
                                        placeSalvoes();

                                      }
             if((gameView.ships.length > 0 && gameView.salvoes.length > 0)){
                                        loadGrid(true)
                                        getSalvoes(gameView.salvoes, gamePlayerId);
                                        placeSalvoes();
                                        $("#add-salvoes").show();
                                        $("#gameHistory").show();
             }

    })


    .catch(function (error) {
              console.log("Request failed: ", error);
     });
}

function getSalvoes(salvoes,playerId){

    let sunks = []

        gameView.salvoes.forEach(salvo => {
            salvo.sunk.forEach(sunk => {
                sunk.shipLocations.forEach(loc => {
                    if(salvo.player == app.currentPlayerId){
                        sunks.push(loc)
                    }
                })
            })
        })

    for(i=0;i< salvoes.length;i++){

        for(j=0; j < salvoes[i].salvoLocations.length; j++){
            let turn = salvoes[i].turn;
            let x = +(salvoes[i].salvoLocations[j].slice(1)) -1;
            let y = salvoes[i].salvoLocations[j].slice(0,1).charCodeAt(0) - 65;
            let cell = document.getElementById("ships"+y+x);

            let isHit = gameView.salvoes[i].hits.indexOf(gameView.salvoes[i].salvoLocations[j]) != - 1 ? true : false;
            let isSink = sunks.indexOf(gameView.salvoes[i].salvoLocations[j]) != -1 ? true : false


                if(gameView.salvoes[i].player == app.currentPlayerId){
                     document.getElementById("salvo"+y+x).classList.contains ("target");
                     document.getElementById("salvo"+y+x).classList.add("water");
                     document.getElementById("salvo"+y+x).innerHTML= "Turn " + turn;

                     if(isHit){
                         document.getElementById("salvo"+y+x).classList.add("shipHit");
                     }
                     if(isSink){
                         document.getElementById("salvo"+y+x).classList.add("sunk"); //pinta celda gris
                     }
                }else{
                       if (cell.classList.contains("busy-cell")) {
                            cell.classList.remove("busy-cell")
                            cell.classList.add("ship-hit");
                            document.getElementById("ships"+y+x).innerHTML= "Turn " + turn;
                       }else{
                               cell.classList.add("fail");
                               document.getElementById("ships"+y+x).innerHTML= "Turn " + turn;
                             }
                       }
          }
    }
    if (app.opponent != " Waiting for player..."){
         historyTable(gameView.hits.opponent, "gameRecordOppTable");
        historyTable(gameView.hits.self, "gameRecordSelfTable");
    }
}

function getSunkenShip(salvoes,playerId){
    let sunk = []

        gameView.salvoes.forEach(salvo => {
            salvo.sunken.forEach(sunk => {
                sunk.location.forEach(loc => {
                    if(salvo.player == app.currentPlayerId){
                        sunk.push(loc)
                    }
                })
            })
        })

}

function getTurn (){
  var arr=[]
  var turn = 0;
  gameView.salvoes.map(function(salvo){
    if(salvo.player == app.currentPlayerId){
      arr.push(salvo.turn);
    }
  })
  turn = Math.max.apply(Math, arr);

  if (turn == -Infinity){
    return 1;
  } else {
    return turn + 1;
  }

}

//PLACE SHIPS
function placeShips(){
    let dataShips = []
    let shipsLoc = document.querySelectorAll(".grid-stack-item");
    console.log(shipsLoc);

    shipsLoc.forEach(function(ship){
        let x = +(ship.dataset.gsX);
        let y = +(ship.dataset.gsY);
        let w = +(ship.dataset.gsWidth);
        let h = +(ship.dataset.gsHeight);
        let shipObject={};
        let location = []
        let type = ship.id;

        if(h>w){
            for(i=0; i< h; i++){
                location.push(String.fromCharCode(65+y+i)+(x+1))
            }
        }else{
            for(i=0; i< w; i++){
                location.push(String.fromCharCode(65+y)+(x+1+i))
            }
        }
        shipObject.shipLocations = location;
        shipObject.shipType = type;

        dataShips.push(shipObject)
    })

    fetch("/api/games/players/" + getParameterByName('gp') + "/ships",{
        method:'POST',
        body: JSON.stringify(dataShips),
        headers: {'Content-Type': 'application/json'}

    })
    .then(function(response) {
        return response.json()
    })
    .then(function(json) {
        console.log("You add ships" + json)
        alert("You add ships")
        window.location.replace("game.html?gp="+ getParameterByName('gp'))

    })
    .catch(function(error){
        console.log(error)
    })
}

// SELECT CELL SALVOES
function placeSalvoes(){

    let salvoCell = document.querySelectorAll(".grid-salvoes .grid-cell");

    for(i=0; i<salvoCell.length; i++){
        salvoCell[i].addEventListener("click", function(){
           selectCell(event);
        })
    }
}
function selectCell(evt){
    let cell = evt.target;
        if(!evt.target.classList.contains('shipHit')){
    		if(document.querySelectorAll('.target').length < 5){
    			evt.target.classList.toggle('target') //toggle -> Cuando solo hay un argumento presente: Alterna el valor de la clase;
    		}else{
    			console.log('to many shots')
    		}
    	} else{
    		console.log('you already have shooted here')
    	}
}

function disableCellsalvo(){

  let salvoCell = document.querySelectorAll(".grid-salvoes .grid-cell");

    for(i=0; i<salvoCell.length; i++){
         salvoCell[i].removeEventListener('click',placeSalvoes());
     }
 }


 //SEND SALVOES BACKEND
  function postSalvoes(){
     var turno = getTurn();
     let postSalvoes = []
     let mysalvoes = document.querySelectorAll(".target");

     gamePlayerId = getParameterByName('gp')
     mysalvoes.forEach(function(salvo){
         let y = salvo.dataset.y; // letter
         let x = +(salvo.dataset.x); // number
         let salvoLocation = y+x;

         postSalvoes.push(salvoLocation);
     })

     fetch("/api/games/players/" + gamePlayerId + "/salvoes",{
         method:'POST',
         body: JSON.stringify(postSalvoes),
         headers: {'Content-Type': 'application/json'}
     })
     .then(res => {
     		if(res.ok){
     			return res.json()
     		}else{
     			return Promise.reject(res.json())
     		}
     })
     .then(function(json) {
           console.log(json)
         alert("you fired your salvoes");

        location.reload();
     })
     .catch(function(error){
        console.log("Request failure: ", error);
          alert('wrong number of salvoes!');
     })
  }

function historyTable (hitsArray, gameRecordTableId) {

        var tableId = "#" + gameRecordTableId + " tbody";
        $(tableId).empty();
        var shipsAfloat = 5;
        var playerTag;
        if (gameRecordTableId == "gameRecordOppTable") {
            playerTag = "#opp";
        }
        if (gameRecordTableId == "gameRecordSelfTable") {
            playerTag = "#";
        }

        hitsArray.forEach(function (playTurn) {
            var hitsReport = "";
            if (playTurn.damages.carrierHits > 0){
                hitsReport += "Carrier " + addDamagesIcons(playTurn.damages.carrierHits, "hit") + " ";
                if (playTurn.damages.carrier === 5){
                    hitsReport += "(SUNK! )";
                    shipsAfloat--;
                }
            }

            if (playTurn.damages.battleshipHits > 0){
                hitsReport += "Battleship " + addDamagesIcons(playTurn.damages.battleshipHits, "hit") + " ";
                if (playTurn.damages.battleship === 4){
                    hitsReport += "(SUNK! )";
                    shipsAfloat--;
                }
            }
            if (playTurn.damages.submarineHits > 0){
                hitsReport += "Submarine " + addDamagesIcons(playTurn.damages.submarineHits, "hit") + " ";
                if (playTurn.damages.submarine === 3){
                    hitsReport += "(SUNK! )";
                    shipsAfloat--;
                }
            }
            if (playTurn.damages.destroyerHits > 0){
                hitsReport += "Destroyer " + addDamagesIcons(playTurn.damages.destroyerHits, "hit") + " ";
                if (playTurn.damages.destroyer === 3){
                    hitsReport += "(SUNK! )";
                    shipsAfloat--;
                }
            }
            if (playTurn.damages.patrolboatHits > 0){
                hitsReport += "Patrol Boat " + addDamagesIcons(playTurn.damages.patrolboatHits, "hit") + " ";
                if (playTurn.damages.patrolboat === 2){
                    hitsReport += "(SUNK! )";
                    shipsAfloat--;
                }
            }

            if (playTurn.missed > 0){
                hitsReport +=  "Missed shots " + addDamagesIcons(playTurn.missed, "missed") + " ";
            }

            if (hitsReport === ""){
                hitsReport = "All salvoes missed! No damages!"
            }

            $('<tr><td class="textCenter">' + playTurn.turn + '</td><td>' + hitsReport + '</td></tr>').prependTo(tableId);

        });
        $('#shipsLeftSelfCount').text(shipsAfloat);
    }

    function addDamagesIcons (numberOfHits, hitOrMissed) {
        var damagesIcons = "";
        if (hitOrMissed === "missed") {
            for (var i = 0; i < numberOfHits; i++) {
                damagesIcons += "<i class='fas fa-certificate' style='color: rgb(252, 187, 41)'></i>"
            }
        }
            if (hitOrMissed === "hit") {
                for (var i = 0; i < numberOfHits; i++) {
                    damagesIcons += "<i class='fas fa-certificate' style='color:red'></i>"
                }
        }
        return damagesIcons;
    }

//////GridStack
const loadGrid = function (ships) {
 document.getElementById("grid").innerHTML = ""
    var options = {
        //matriz 10 x 10
        width: 10,
        height: 10,
        verticalMargin: 0,
        cellHeight: 45,
        disableResize: true,
        float: true,
        disableOneColumnMode: true,
        // en falso permite arrastrar a los widget, true lo deniega
        staticGrid: ships,
        //para animaciones
        animate: true
    }
    //inicializacion de la matriz
    $('.grid-stack').gridstack(options);

    grid = $('#grid').data('gridstack');

    //Aqui se inicializan los widgets(nuestros barcos) en la matriz
    //.addWidget(elemento,pos x, pos y, ancho, alto) **

    if(ships){
            for(i=0;i<gameView.ships.length;i++){
                let shipType = gameView.ships[i].shipType;
                let x = +(gameView.ships[i].shipLocations[0].slice(1)) - 1;
                let y = gameView.ships[i].shipLocations[0].slice(0,1).toUpperCase().charCodeAt(0)-65;
                let w; //width
                let h; //height

                if(gameView.ships[i].shipLocations[0].slice(0,1) == gameView.ships[i].shipLocations[1].slice(0,1)){
                    w = gameView.ships[i].shipLocations.length;
                    h = 1;
                    grid.addWidget($('<div id="'+shipType+'"><div class="grid-stack-item-content '+shipType+'Horizontal"></div><div/>'), x, y, w, h);

                } else{
                    h = gameView.ships[i].shipLocations.length;
                    w = 1;
                    grid.addWidget($('<div id="'+shipType+'"><div class="grid-stack-item-content '+shipType+'Vertical"></div><div/>'), x, y, w, h);
                }
		    }
     } else{
                grid.addWidget($('<div id="patrolBoat"><div class="grid-stack-item-content patrolBoatHorizontal"></div><div/>'),0, 1, 2, 1);

                grid.addWidget($('<div id="carrier"><div class="grid-stack-item-content carrierHorizontal"></div><div/>'),1, 5, 5, 1);

                grid.addWidget($('<div id="battleship"><div class="grid-stack-item-content battleshipHorizontal"></div><div/>'),3, 1, 4, 1);

                grid.addWidget($('<div id="submarine"><div class="grid-stack-item-content submarineVertical"></div><div/>'),8, 2, 1, 3);

                grid.addWidget($('<div id="destroyer"><div class="grid-stack-item-content destroyerHorizontal"></div><div/>'),7, 8, 3, 1);
            }


    //createGrid construye la estructura de la matriz
    createGrid(11, $(".grid-ships"), 'ships')
    /*SALVOES GRID*/
    createGrid(11, $(".grid-salvoes"), 'salvo')

    //Inicializo los listenener para rotar los barcos, el numero del segundo argumento
    //representa la cantidad de celdas que ocupa tal barco
 if(!ships){
    rotateShips("carrier", 5)
    rotateShips("battleship", 4)
    rotateShips("submarine",3)
    rotateShips("destroyer", 3)
    rotateShips("patrolBoat",2)
 }
    listenBusyCells('ships')
    $('.grid-stack').on('change', () => listenBusyCells('ships'))

}


//createGrid construye la estructura de la matriz
const createGrid = function(size, element, id){
    if(document.getElementById(id+"wrapper")){
        document.getElementById(id+"wrapper").innerHTML = ""

    }
    // definimos un nuevo elemento: <div></div>
    let wrapper = document.createElement('DIV')
    wrapper.id = id+"wrapper"

    // le agregamos la clase grid-wrapper: <div class="grid-wrapper"></div>
    wrapper.classList.add('grid-wrapper')

    //vamos armando la tabla fila por fila
    for(let i = 0; i < size; i++){
        //row: <div></div>
        let row = document.createElement('DIV')
        //row: <div class="grid-row"></div>
        row.classList.add('grid-row')
        //row: <div id="ship-grid-row0" class="grid-wrapper"></div>
        row.id =`${id}-grid-row${i}`

        wrapper.appendChild(row)

        for(let j = 0; j < size; j++){
            //cell: <div></div>
            let cell = document.createElement('DIV')
            //cell: <div class="grid-cell"></div>
            cell.classList.add('grid-cell')
            //aqui entran mis celdas que ocuparan los barcos
            if(i > 0 && j > 0){
                //cell: <div class="grid-cell" id="ships00"></div>
                cell.id = `${id}${i - 1}${ j - 1}`
                cell.dataset.y = String.fromCharCode(i - 1 + 65)
                cell.dataset.x = j
            }
            //aqui entran las celdas cabecera de cada fila
            if(j===0 && i > 0){
                // textNode: <span></span>
                let textNode = document.createElement('SPAN')

                textNode.innerText = String.fromCharCode(i+64)
                //cell: <div class="grid-cell" id="ships00"></div>
                cell.appendChild(textNode)
            }
            // aqui entran las celdas cabecera de cada columna
            if(i === 0 && j > 0){
                // textNode: <span>A</span>
                let textNode = document.createElement('SPAN')
                // 1
                textNode.innerText = j
                //<span>1</span>
                cell.appendChild(textNode)
            }

            row.appendChild(cell)
        }
    }

    element.append(wrapper)
}

const rotateShips = function(shipType, cells){

        $(`#${shipType}`).click(function(){
            //document.getElementById("alert-text").innerHTML = `Rotaste: ${shipType}`
            console.log($(this))
            //Establecemos nuevos atributos para el widget/barco que giramos
            let x = +($(this).attr('data-gs-x'))
            let y = +($(this).attr('data-gs-y'))

        if($(this).children().hasClass(`${shipType}Horizontal`)){
            // grid.isAreaEmpty revisa si un array esta vacio**
            // grid.isAreaEmpty(fila, columna, ancho, alto)
        	if(grid.isAreaEmpty(x,y+1,1,cells) || y + cells < 10){
	            if(y + cells - 1 < 10){
                    // grid.resize modifica el tamaño de un array(barco en este caso)**
                    // grid.resize(elemento, ancho, alto)
	                grid.resize($(this),1,cells);
	                $(this).children().removeClass(`${shipType}Horizontal`);
	                $(this).children().addClass(`${shipType}Vertical`);
	            } else{
                        /* grid.update(elemento, fila, columna, ancho, alto)**
                        este metodo actualiza la posicion/tamaño del widget(barco)
                        ya que rotare el barco a vertical, no me interesa el ancho sino
                        el alto
                        */
	            		grid.update($(this), null, 10 - cells)
	                	grid.resize($(this),1,cells);
	                	$(this).children().removeClass(`${shipType}Horizontal`);
	                	$(this).children().addClass(`${shipType}Vertical`);
	            }
            }else{
            		document.getElementById("alert-text").innerHTML = "A ship is blocking the way!"
            }

        //Este bloque se ejecuta si el barco que queremos girar esta en vertical
        }else{

            if(x + cells - 1  < 10){
                grid.resize($(this),cells,1);
                $(this).children().addClass(`${shipType}Horizontal`);
                $(this).children().removeClass(`${shipType}Vertical`);
            } else{
                /*en esta ocasion para el update me interesa el ancho y no el alto
                ya que estoy rotando a horizontal, por estoel tercer argumento no lo
                declaro (que es lo mismo que poner null o undefined)*/
                grid.update($(this), 10 - cells)
                grid.resize($(this),cells,1);
                $(this).children().addClass(`${shipType}Horizontal`);
                $(this).children().removeClass(`${shipType}Vertical`);
            }

        }
    });

}

//Bucle que consulta por todas las celdas para ver si estan ocupadas o no
const listenBusyCells = function(id){
    /* id vendria a ser ships. Recordar el id de las celdas del tablero se arma uniendo
    la palabra ships + fila + columna contando desde 0. Asi la primer celda tendra id
    ships00 */
    for(let i = 0; i < 10; i++){
        for(let j = 0; j < 10; j++){
            if(!grid.isAreaEmpty(i,j)){
                $(`#${id}${j}${i}`).addClass('busy-cell').removeClass('empty-cell')
            } else{
                $(`#${id}${j}${i}`).removeClass('busy-cell').addClass('empty-cell')
            }
        }
    }
}
