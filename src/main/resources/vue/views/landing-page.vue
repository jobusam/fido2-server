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
                fetch('/api/device/start-authentication',{method: 'POST',
                        body: JSON.stringify({username: this.username})})
                    .then(response => response.json())
                    .then(assertionRequest => assertionRequest.publicKeyCredentialRequestOptions)
                    .then(credentialOptions => {
                        credentialOptions.challenge = Base64.toUint8Array(credentialOptions.challenge);
                        credentialOptions.allowCredentials
                            .map( cred => {
                                cred.id = Base64.toUint8Array(cred.id);
                                return cred;
                            });
                        console.log("PublicKeyCredentialRequestOptions = ",credentialOptions)
                        return credentialOptions;
                        })
                    .then(publicKeyCredentialRequestOptions => {
                        navigator.credentials.get({publicKey: publicKeyCredentialRequestOptions})
                            .then(creds => {
                                console.log("Credentials =",creds)
                                let content = {
                                    id : creds.id,
                                    response : {
                                         authenticatorData : Base64.fromUint8Array(new Uint8Array(creds.response.authenticatorData), true),
                                         clientDataJSON : Base64.fromUint8Array(new Uint8Array(creds.response.clientDataJSON), true),
                                         signature : Base64.fromUint8Array(new Uint8Array(creds.response.signature), true)
                                        },
                                    clientExtensionResults : creds.getClientExtensionResults(),
                                    type : creds.type
                                };
                                console.log("Credentials JSON = ",JSON.stringify(content));
                                fetch('/api/device/finish-authentication', {method: 'POST', body: JSON.stringify(content)})
                                    .then(response => {
                                        if(response.ok){
                                           console.log("Request complete! response:", response);
                                           location.reload();
                                        }else{
                                            console.log("Authentication failed with HTTP status: ",response.status);
                                        }
                                     })
                                    .catch(error => console.log("Finish authentication request error: ",error));
                            })
                            .catch(error => console.log("Error validating user: ",error));
                    })
                    .catch(rejected => console.log("Getting assertionRequest failed",rejected));
            }
        }
    });
</script>