Vue.config.devtools = true;


function getData(){
    return fetch("/api/games")
    .then(function(response) {
         if (response.ok) {
            return response.json();
         }
         throw new Error(response.statusText);
    })
    .then(function(data) {
        console.log(data);
        app.games = data.Games;
        //data.Games.reverse();
        app.currentPlayer = data.Player;
        //console.log(app.games);
    })
}

getData();

let app = new Vue({
    el: '#app',
    data: {
        data:"",
        scores:[],
        games: [],
        currentPlayer:[],
        players: [],
        username: "",
        password: "",
        player:"",
     },
    created: function () {
            this.getScores();
    },
    methods: {

          login: function () {
             $.post("/api/login", {
               username:app.username,
               password:app.password
             })
             .done(function () {
               console.log("Logged In!");
               //getData();
               window.location.reload();
              })
            .fail(function() {
           	    alert('Wrong user name or password,try again!');
           	       $("#username").val("");
                   $("#password").val("");
                   $("#username").focus("");
               });
         },
         logout: function () {
           $.post("/api/logout")
           .done(function() {
              console.log("Logged out");
              alert('Logging out of your session'),
               $("#username").val("");
               $("#password").val("");
               $("#hi-user").html("Hello Guest!");
               $("#logout-btn").hide();
               getData();
               //window.location.reload();
              })
         },
         newGame: function () {
            	 $.post("/api/games")
            	.done(function (response) {
            	    console.log("Request success: ", response.data);
            	   location.assign("/web/game.html?gp=" + response.gpid);
            	    $("#ship-grid").hide();
            	    $("#salvo-grid").hide();
            	})
            	.fail(function (data) {
                     console.log("game creation failed");
                     let obj = JSON.parse(data.responseText);
                     alert(obj.error);
                })
          },
          signup: function () {
                fetch("/api/players", {
                 credentials: 'include',
                 headers: {
                   'Content-Type': 'application/x-www-form-urlencoded'
                 },
                  method: 'POST',
                   body: 'username=' + this.username + '&password=' + this.password,
                })
                .then(function (res) {
                  return res.json();
                })
                .then(function (data) {
                   console.log('Request success: ', data);
                 if (data.username) {
                    alert("Welcome " + app.username);
                    app.login();
                 } else {
                   alert("Wrong username or password, try again");
                 }
                 })
                 .catch(function (error) {
                  console.log('Request failure: ',  error.message);
                  });
                 },

         getDate: function(data) {
                return new Date(data.created).toLocaleString();
         },
         join: function (game_id) {
                     fetch("/api/game/" + game_id + "/players", {
                             credentials: "include",
                             headers: {
                                 "Content-Type": "application/x-www-form-urlencoded"
                             },
                             method: "POST"
                     })
                     .then(data => {
                             console.log("Request success: ", data);
                             return data.json();
                     })
                     .then(json => {
                             console.log(json);
                             window.location = "/web/game.html?gp=" + json.gpid
                     })
                     .catch(function (error) {
                             console.log("Request failure: ", error);
                     });
          },
          getScores: function(){
                     fetch("/api/leaderBoard/")
                     .then((res) => res.json())
                     .then((json) => {
                       this.scores = json;
                       console.log(this.scores);
                     })
                     .catch(function (error) {
                       console.log('Request failure: ', error);
                     });
          },
    }

});

