<template id="user-profile">
    <app-frame>
        <div>
            <ul v-if="user">
                <dt>User ID</dt>
                <dd>{{user.id}}</dd>
                <dt>Name</dt>
                <dd>{{user.name}}</dd>
                <dt>Email</dt>
                <dd>{{user.email}}</dd>
                <dt>Application Roles</dt>
                <dd>{{user.roles}}</dd>
            </ul>
            <!-- userDetails can be empty! -->
            <ul v-if="user && user.userDetails">
                <dt>Birthday</dt>
                <dd>{{user.userDetails.dateOfBirth}}</dd>
                <dt>Address</dt>
                <dd>{{user.userDetails.address}}</dd>
            </ul>
        </div>
        <div>
            <button v-on:click="deviceRegistration">register device (FIDO2)</button>
        </div
    </app-frame>
</template>
<script>
    Vue.component("user-profile", {
        template: "#user-profile",
        data: () => ({
            user: null,
        }),
        created() {
            const userId = this.$javalin.pathParams["user-id"];
            fetch(`/api/users/${userId}`)
                .then(res => res.json())
                .then(res => this.user = res)
                .catch(() => alert("Error while fetching user"));
        },
        methods: {
            deviceRegistration: function () {
                fetch('/api/device/init-registration',{method: 'GET'})
                    .then(res => res.json())
                    .then(createOptions => {
                        //decode challenge and user id from Base64URL format
                        createOptions.user.id = Base64.toUint8Array(createOptions.user.id);
                        createOptions.challenge = Base64.toUint8Array(createOptions.challenge);
                        console.log(createOptions);
                        return createOptions;
                        })
                    .then(convertedOptions => {
                         navigator.credentials.create({publicKey: convertedOptions})
                            .then(creds =>{
                                let content = {
                                    id : creds.id,
                                    response: {
                                        attestationObject : Base64.fromUint8Array(new Uint8Array(creds.response.attestationObject), true),
                                        clientDataJSON : Base64.fromUint8Array(new Uint8Array(creds.response.clientDataJSON), true)
                                        },
                                    clientExtensionResults: creds.getClientExtensionResults(),
                                    type : creds.type
                                };
                                console.log(content);
                                fetch('/api/device/finish-registration', {
                                    method: 'POST', body: JSON.stringify(content) })
                                    .then(res => console.log("Request complete! response:", res));
                                });
                        });
            }
        }
    });
</script>