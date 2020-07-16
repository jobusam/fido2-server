<template id="landing-page">
    <app-frame>
        <h2 class="landing-page">Organize your finances</h2>
        <div v-if="$javalin.state.currentUser == 'anonym'">
            <h2>Login</h2>
            <form action="/login" method="post">
                <input type="text" name="username" placeholder="Username"/>
                <input type="password" name="password" placeholder="Password"/>
                <button type="submit" >Login</button>
            </form>
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
        template: "#landing-page", });
</script>