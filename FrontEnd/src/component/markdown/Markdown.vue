<template>
  <div class="container">
    <h1 class="topic">
      {{ language === 'zh-CN' ? '在线协作Markdown编辑器' : 'Collaborative Markdown Editor' }}
    </h1>
    <div class="button-group">
      <button class="theme-button" :class="{ dark: theme === 'dark' }" @click="toggleTheme">
        {{ theme === 'light' ? 'Dark' : 'Light' }}
      </button>
      <button class="language-button" :class="{ active: language === 'zh-CN' }" @click="toggleLanguage(language)">
        {{ language === 'zh-CN' ? 'English' : '中文' }}
      </button>
      <button class="language-button" onclick="window.location.href = '/';">
        {{ language === 'zh-CN' ? 'Back' : '返回' }}
      </button>
    </div>
    <div class="status-container">
      <div class="status-dot"
           :class="{ 'green-dot': isConnected, 'red-dot': !isConnected }"></div>
    </div>
    <div class="online">
      Online: {{ online }}
    </div>
    <MdEditor v-model="text" :theme="theme" :language="language" :toolbars="toolbars" no-upload-img
              ref="editorRef">
      <template #defToolbars>
        <Emoji/>
        <ExportPDF :modelValue="text"/>
      </template>
    </MdEditor>
  </div>
</template>

<script setup lang="ts">
import {nextTick, onMounted, ref, watch} from 'vue';
import type {ExposeParam} from 'md-editor-v3';
import {MdEditor} from 'md-editor-v3';
import {Emoji, ExportPDF} from '@vavt/v3-extension';
import {toolbars} from './staticConfig';
import {getCursorPos, updateCursorPosition} from './cursor';
import {OperationList} from './OperationList';
import DiffMatchPatch from "diff-match-patch";

import '@vavt/v3-extension/lib/asset/style.css';
import 'md-editor-v3/lib/style.css';
import './Markdown.css';

let theme = ref('light');
let language = ref('en-US');
let text = ref('');
let opList = new OperationList();
let online = ref('0');
const editorRef = ref<ExposeParam>();

let socket = null;
let isConnected = ref(false);

let enableOnChange = true;

function toggleTheme() {
  theme.value = theme.value === 'light' ? 'dark' : 'light';
  if (theme.value === 'dark') {
    document.body.style.backgroundColor = 'black';
    document.body.style.color = 'white';
  } else {
    document.body.style.backgroundColor = '';
    document.body.style.color = '';
  }
}

function toggleLanguage(lang) {
  if (lang === 'en-US') {
    language.value = 'zh-CN';
  } else {
    language.value = 'en-US';
  }
}

function findStringChanges(original: string, modified: string) {
  const dmp = new DiffMatchPatch();
  const diffs = dmp.diff_main(original, modified);
  dmp.diff_cleanupSemantic(diffs);

  const changes: Array<{ type: string; position: number; content: string; timestamp: number }> = [];
  let currentPosition = 0;
  let timestamp = Date.now();

  for (const [op, text] of diffs) {
    const change = {
      type: "",
      position: currentPosition,
      content: text,
      timestamp: timestamp,
    };

    if (op === 0) {
      currentPosition += text.length;
    } else if (op === 1) {
      change.type = "insert";
      timestamp += 1;
      changes.push(change);
      currentPosition += text.length;
    } else if (op === -1) {
      change.type = "delete";
      timestamp += 1;
      changes.push(change);
    } else if (op === -1) {
      change.type = "edit";
      timestamp += 1;
      changes.push(change);
      currentPosition += text.length;
    }
  }

  return changes;
}

function calculateStringFromOperations(operationList: OperationList) {
  let str = "";
  for (const operation of operationList.getSortedOperations()) {
    const {type, position, content} = operation;

    if (type === "insert") {
      str = str.slice(0, position) + content + str.slice(position);
    } else if (type === "delete") {
      const endIndex = position + content.length;
      str = str.slice(0, position) + str.slice(endIndex);
    }
  }

  return str;
}

watch(text, (newValue, oldValue) => {
  if (!enableOnChange) {
    return;
  }

  const operation = findStringChanges(oldValue, newValue);
  operation.forEach((op) => {
    console.log(op);
    opList.add(op);
    if (online.value !== '0') {
      socket.send(JSON.stringify(op));
    }
  });
});

onMounted(() => {
  document.title = 'Markdown Editor';

  const urlParams = new URLSearchParams(window.location.search);
  let socketURL = 'ws://';
  socketURL += window.location.host;
  //TODO
  socketURL = 'ws://localhost:12345'
  socketURL += '/MDHandler/';
  const id = urlParams.get('id');
  if (id == null) {
    alert('Please enter a valid id.');
  }
  socketURL += id;
  console.log('socketURL:', socketURL);
  socket = new WebSocket(socketURL);

  socket.addEventListener('message', (event) => {
    const message = JSON.parse(event.data);
    console.log('message:', message);
    if (message.type === 1) {
      online.value = message.message;
    } else if (message.type === 2) {
      // Init or Error
      enableOnChange = false;
      opList.reset(message.message);
      let preCursorPos = getCursorPos(text);
      let preText = text.value;
      const newText = calculateStringFromOperations(opList);
      text.value = newText;

      if (preText.length === 0) {
        preCursorPos = message.message.length;
      }

      preCursorPos = updateCursorPosition(preText, newText, preCursorPos);
      preCursorPos = preCursorPos > newText.length ? newText.length : preCursorPos;
      nextTick(() => {
        console.info('Configuring cursor position:', preCursorPos);
        const option = {
          cursorPos: preCursorPos,
        };
        editorRef.value?.focus(option);

        enableOnChange = true;
      });
    } else if (message.type === 3) {
      // New Ops
      enableOnChange = false;
      const newOp = message.message;
      opList.add(newOp);
      let preCursorPos = getCursorPos(text);
      let preText = text.value;
      const newText = opList.getString();
      text.value = newText;

      if (preText.length === 0) {
        preCursorPos = message.message.length;
      }

      preCursorPos = updateCursorPosition(preText, newText, preCursorPos);
      preCursorPos = preCursorPos > newText.length ? newText.length : preCursorPos;
      nextTick(() => {
        console.info('Configuring cursor position:', preCursorPos);
        const option = {
          cursorPos: preCursorPos,
        };
        editorRef.value?.focus(option);

        enableOnChange = true;
      });
    }
  });

  socket.addEventListener('open', () => {
    isConnected.value = true;
  });

  socket.addEventListener('close', () => {
    isConnected.value = false;
  });
});
</script>

<script lang="ts">
export default {
  name: 'Markdown'
};
</script>