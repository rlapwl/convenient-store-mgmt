<template>

<v-data-table
    :headers="headers"
    :items="orderStatus"
    :items-per-page="5"
    class="elevation-1"
  ></v-data-table>

</template>

<script>
  const axios = require('axios').default;

  export default {
    name: 'OrderStatus',
    props: {
      value: Object,
      editMode: Boolean,
      isNew: Boolean
    },
    data: () => ({
        headers: [
            { text: "id", value: "id" },
            { text: "productId", value: "productId" },
            { text: "status", value: "status" },
            { text: "quantity", value: "quantity" },
        ],
        orderStatus : [],
    }),
    async created() {
      var temp = await axios.get(axios.backend + '/orderstatuses')

      this.orderStatus = temp.data._embedded.orderstatuses;

    },
    methods: {
    }
  }
</script>

