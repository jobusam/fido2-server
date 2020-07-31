<template id="app-frame">
    <div class="app-frame">
        <header>
            <span>Fido 2 - Test</span>
            <span v-if="$javalin.state.currentUser != 'anonym'">
                <span>Current user: '{{$javalin.state.currentUser}}'</span>
                <span><a href="/api/logout"><button>Logout</button></a></span>
            </span>
        </header>
        <slot></slot>
    </div>
</template>
<script>
    Vue.component("app-frame", {
        template: "#app-frame",
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