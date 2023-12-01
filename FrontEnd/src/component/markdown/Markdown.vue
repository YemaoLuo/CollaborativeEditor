<template>
  <div class="container">
    <h1 class="topic">
      {{ language === 'zh-CN' ? '在线协作Markdown编辑器' : 'Collaborative Markdown Editor' }}
    </h1>
    <div class="button-group">
      <button class="theme-button" :class="{ dark: theme === 'dark' }" @click="toggleTheme()">
        {{ themeText }}
      </button>
      <button class="language-button" :class="{ active: language === 'zh-CN' }" @click="toggleLanguage(language)">
        {{ language === 'zh-CN' ? 'English' : '中文' }}
      </button>
      <button class="language-button" onclick="window.location.href = '/';">
        {{ language === 'zh-CN' ? '返回' : 'Back' }}
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
import {MdEditor} from 'md-editor-v3';
import {Emoji, ExportPDF} from '@vavt/v3-extension';
import {toolbars} from './staticConfig';
import {getCursorPos, updateCursorPosition} from './cursor';
import {OperationList} from './OperationList';
import DiffMatchPatch from 'diff-match-patch';

import '@vavt/v3-extension/lib/asset/style.css';
import 'md-editor-v3/lib/style.css';
import './Markdown.css';

const theme = ref('light');
const themeText = ref('Dark');
const language = ref('en-US');
const text = ref('');
const opList = new OperationList();
const online = ref('0');
const editorRef = ref();
const latestTimestamp = ref(Date.now());

let socket = null;
const isConnected = ref(false);

let enableOnChange = true;

function toggleTheme() {
  theme.value = theme.value === 'light' ? 'dark' : 'light';

  if (theme.value === 'dark') {
    themeText.value = language.value === 'zh-CN' ? '明亮' : 'Light';
  } else {
    themeText.value = language.value === 'zh-CN' ? '暗黑' : 'Dark';
  }

  updateBodyStyles();
}

function toggleLanguage() {
  language.value = language.value === 'zh-CN' ? 'en-US' : 'zh-CN';

  if (theme.value === 'dark') {
    themeText.value = language.value === 'zh-CN' ? '明亮' : 'Light';
  } else {
    themeText.value = language.value === 'zh-CN' ? '暗黑' : 'Dark';
  }

  updateBodyStyles();
}

function updateBodyStyles() {
  document.body.style.backgroundColor = theme.value === 'dark' ? 'black' : '';
  document.body.style.color = theme.value === 'dark' ? 'white' : '';
}

function findStringChanges(original, modified) {
  const dmp = new DiffMatchPatch();
  const diffs = dmp.diff_main(original, modified);
  dmp.diff_cleanupSemantic(diffs);

  const changes = [];
  let currentPosition = 0;
  let timestamp = Date.now();

  for (const [op, text] of diffs) {
    const change = {
      type: '',
      position: currentPosition,
      content: text,
      timestamp: timestamp,
      latestTimestamp: 0,
    };

    switch (op) {
      case 0:
        currentPosition += text.length;
        break;
      case 1:
        change.type = 'insert';
        timestamp += 1;
        changes.push(change);
        currentPosition += text.length;
        break;
      case -1:
        change.type = 'delete';
        timestamp += 1;
        changes.push(change);
        break;
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
  if (!enableOnChange) return;

  const operations = findStringChanges(oldValue, newValue);

  operations.forEach((op) => {
    op.latestTimestamp = latestTimestamp.value;
    console.log(op);
    opList.add(op);

    if (online.value !== '0' && socket) {
      socket.send(JSON.stringify(op));
    }
  });
});

onMounted(() => {
  document.title = 'Markdown Editor';

  const urlParams = new URLSearchParams(window.location.search);
  // TODO
  // const socketURL = `ws://${window.location.host}/MDHandler/${urlParams.get('id')}`;
  const socketURL = `ws://localhost:12345/MDHandler/${urlParams.get('id')}`;
  console.log('socketURL:', socketURL);
  socket = new WebSocket(socketURL);

  socket.addEventListener('message', (event) => {
    const message = JSON.parse(event.data);
    console.log('message:', message);

    if (message.type === 1) {
      online.value = message.message;
    } else {
      enableOnChange = false;
      let preCursorPos = getCursorPos(text.value);
      let preText = text.value;
      let newText;

      if (message.type === 2) {
        opList.reset(message.message);
        newText = calculateStringFromOperations(opList);
      } else if (message.type === 3) {
        opList.add(message.message);
        newText = opList.getString();
      }

      latestTimestamp.value = opList.getLocalTimestamp();
      text.value = newText;

      if (preText.length === 0) {
        preCursorPos = message.message.length;
      }

      preCursorPos = updateCursorPosition(preText, newText, preCursorPos);
      preCursorPos = preCursorPos > newText.length ? newText.length : preCursorPos;

      nextTick(() => {
        console.info('Configuring cursor position:', preCursorPos);
        editorRef.value?.focus({cursorPos: preCursorPos});
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