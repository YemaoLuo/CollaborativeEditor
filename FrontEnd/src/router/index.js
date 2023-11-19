import { createRouter, createWebHistory } from 'vue-router'
import Index from "@/component/index/Index.vue";
import Markdown from "@/component/markdown/Markdown.vue";

const routes = [
    {
        path: '/',
        name: 'Index',
        component: Index
    },
    {
        path: '/markdown',
        name: 'Markdown',
        component: Markdown
    }
]

const router = createRouter({
    history: createWebHistory('/'),
    routes
})

export default router
