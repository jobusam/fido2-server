<template id="landing-page">
    <app-frame>
        <h2 class="landing-page">Organize your finances</h2>
        <div v-if="$javalin.state.currentUser == 'anonym'">
            <h2>Login with Password</h2>
            <form action="/api/login" method="post">
                <input type="text" name="username" placeholder="Username"/>
                <input type="password" name="password" placeholder="Password"/>
                <button type="submit" >Login</button>
            </form>
            <br>
            <h2>Login with Authentication Device (like Nitrokey FIDO2)</h2>
            <input type="text", name="name",placeholder="Username"/>
            <button v-on:click="authWithDevice">login</button>
        </div>
        <div v-else>
            <a href="/users">
                <button>Go to User Profiles</button>
            </a>
        </div>
    </app-frame>
</template>
<script>
    Vue.component("landing-page", {
        template: "#landing-page",
        methods: {
          authWithDevice: function () {
                alert("Use device login")
                fetch('/api/device/start-authentication',{method: 'GET'})
                    .then(res => res.json())
                    .then(assertionRequest => {
                      console.log("Assertion Request = ",assertionRequest)
                    })
                    .catch(rejected => console.log("Getting assertionRequest failed",rejected));
            }
        }
    });
</script>