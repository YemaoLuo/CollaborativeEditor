<template>
  <div class="container">
    <h1 class="topic">Collaborative Editor</h1>
    <div class="input-container">
      <input id="id" type="text" placeholder="DrawingBoard or MarkDown ID">
      <button id="enter" @click="enterDB">Enter DrawingBoard</button>
      <button id="enter" @click="enterMD">Enter MarkDown</button>
    </div>
  </div>
</template>

<script>
import '@/component/index/Index.css';
import {useRouter} from "vue-router";

export default {
  name: 'App',
  setup() {
    const router = useRouter();

    const enterMD = () => {
      const id = document.getElementById('id').value;
      console.log('enter id=' + id);
      if (id === '') {
        alert('Please enter a valid id.');
        return;
      }
      router.push({ path: '/markdown', query: { id } });
    };

    const enterDB = () => {
      const id = document.getElementById('id').value;
      console.log('enter id=' + id);
      if (id === '') {
        alert('Please enter a valid id.');
        return;
      }
      window.location.href = '/drawingBoard/board.html?id=' + id;
    };

    return {
      enterMD,
      enterDB,
    };
  },
  created() {
    document.title = 'Collaborative Editor';
  },
};
</script>