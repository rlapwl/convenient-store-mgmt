
import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router);


import OrderManager from "./components/OrderManager"

import OrderStatus from "./components/OrderStatus"
import DeliveryManager from "./components/DeliveryManager"

import ProductManager from "./components/ProductManager"

import ProductPage from "./components/ProductPage"
import PaymentManager from "./components/PaymentManager"

import MessageManager from "./components/MessageManager"

export default new Router({
    // mode: 'history',
    base: process.env.BASE_URL,
    routes: [
            {
                path: '/orders',
                name: 'OrderManager',
                component: OrderManager
            },

            {
                path: '/orderStatuses',
                name: 'OrderStatus',
                component: OrderStatus
            },
            {
                path: '/deliveries',
                name: 'DeliveryManager',
                component: DeliveryManager
            },

            {
                path: '/products',
                name: 'ProductManager',
                component: ProductManager
            },

            {
                path: '/productPages',
                name: 'ProductPage',
                component: ProductPage
            },
            {
                path: '/payments',
                name: 'PaymentManager',
                component: PaymentManager
            },

            {
                path: '/messages',
                name: 'MessageManager',
                component: MessageManager
            },



    ]
})
