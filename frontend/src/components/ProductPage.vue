<template>

<v-data-table
    :headers="headers"
    :items="productPage"
    :items-per-page="5"
    class="elevation-1"
  ></v-data-table>

</template>

<script>
  const axios = require('axios').default;

  export default {
    name: 'ProductPage',
    props: {
      value: Object,
      editMode: Boolean,
      isNew: Boolean
    },
    data: () => ({
        headers: [
            { text: "id", value: "id" },
            { text: "quantity", value: "quantity" },
            { text: "price", value: "price" },
        ],
        productPage : [],
    }),
    async created() {
      var temp = await axios.get(axios.backend + '/productpages')

      this.productPage = temp.data._embedded.productpages;

    },
    methods: {
    }
  }
</script>

