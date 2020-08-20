<template id="app-frame">
    <div class="app-frame">
        <header>
            <span>Fido 2 - Test</span>
            <span v-if="$javalin.state.currentUser != 'anonym'">
                <span>Current user: '{{$javalin.state.currentUser}}'</span>
                <span><button v-on:click="logout">Logout</button></span>
            </span>
        </header>
        <slot></slot>
    </div>
</template>
<script>
    Vue.component("app-frame", {
        template: "#app-frame",
        methods: {
            logout: function () {
                fetch('/api/logout')
                    .then(response => {
                        if(!response.ok){
                            throw new Error("Logout failed with HTTP Status: ",response.status)
                        }
                        location.assign('/');
                    })
                    .catch(rejected => console.log("Logout failed",rejected));
            }
        }
    });
</script>
<style>
    .app-frame > header {
        padding: 20px;
        background: #b6e2ff;
        font-size: 20px;
        display: flex;
        justify-content: space-between;
        align-items: center;
    }
</style>