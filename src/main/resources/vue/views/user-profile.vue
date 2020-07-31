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
                        fetch('/api/deviceregistration/',{method: 'POST'})
                        .then(res => res.json())
                        .then(createOptions => {
                                console.log(createOptions);
                                //convert b64-encoded values in Uint8Arrays, otherwise firefox will throw a type error!
                                //decodedChallenge = atob(createOptions.challenge)
                                //console.log("encodedChallenge = " + decodedChallenge)
                                //console.log("decodedId = " + decodedId)
                                //createOptions.challenge = Uint8Array.from(createOptions.challenge, c => c.charCodeAt(0));
                                createOptions.user.id = Uint8Array.from(atob(createOptions.user.id), c => c.charCodeAt(0));

                                //const credential = navigator.credentials.create({
                                //        publicKey: createOptions
                                //});
                                //console.log(credential);
                        });
                    }
                }
    });
</script>