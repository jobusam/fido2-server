<template id="landing-page">
    <app-frame>
        <h2 class="landing-page">Organize your finances</h2>
        <div v-if="$javalin.state.currentUser == 'anonym'">
            <h2>Login with Password</h2>
            <input type="text" v-model="username">
            <input type="password" v-model="password">
            <p>
            <button v-on:click="authWithPassword">Login with Password</button>
            <p>
            <button v-on:click="authWithDevice">Login with Device (FIDO2)</button>
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
        data: () => ({
            username: '',
            password: ''
        }),
        methods: {
            authWithPassword: function () {
                fetch('/api/login', {method: 'POST',
                        body: JSON.stringify({username: this.username, password: this.password})})
                    .then(response => {
                        if(!response.ok){
                            throw new Error("Login failed with HTTP Status: ",response.status)
                        }
                        console.log("Login was successful! ",response);
                        //Reload webpage to update Javalin State Function
                        //FIXME: in future this reload should be avoided and maybe also the javalin state function!
                        location.reload();
                    })
                    .catch(rejected => console.log("Login failed",rejected));
            },
            authWithDevice: function () {
                console.log("Use login with device. username = ",this.username);
                //fetch('/api/device/start-authentication',{method: 'GET'})
                //    .then(res => res.json())
                //    .then(assertionRequest => {
                //      console.log("Assertion Request = ",assertionRequest)
                //    })
                //    .catch(rejected => console.log("Getting assertionRequest failed",rejected));
            }
        }
    });
</script>