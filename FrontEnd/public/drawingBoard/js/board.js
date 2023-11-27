let canvas = document.getElementById("drawing-board");
let ctx = canvas.getContext("2d");
let penColor = 'black';
let penColor2 = penColor;
let penWidth = 2;
let shapeLineWidth = document.getElementById("shape-lineWidth");
let lineWidthHidden = document.getElementById("lineWidth-hidden");
let flag = false;
let eraserState = false;
let eraser = document.getElementById("eraser")
let pen = document.getElementById("pen")
let historyData = [];
let penColorSelected = document.querySelector('.penColor')
let penColorHidden = document.getElementById('penColor-hidden')
let timer = null
let save = document.getElementById('save')
let dot = document.getElementById('dot');

let socket = null;
const urlParams = new URLSearchParams(window.location.search);
let socketURL = 'ws://';
const id = urlParams.get('id');
if (id === null) {
    alert("Please enter a valid id.")
}
console.log("onload id=" + id);
const url = "ws://" + window.location.host + "/DrawingHandler/" + id;
// const url = "ws://" + "127.0.0.1:12345" + "/DrawingHandler/" + id;
console.log("websocket url:" + url);
socket = new WebSocket(url);

pen.classList.add('active');
ctx.willReadFrequently = true;


function canvasSetSize() {
    let pageWidth = window.innerWidth;
    let pageHeight = window.innerHeight;
    canvas.width = pageWidth;
    canvas.height = pageHeight;
}

function autoSetSize() {
    canvasSetSize();
    window.onresize = function () {
        canvasSetSize();
    }
}

autoSetSize();

penColorSelected.onclick = function () {
    if (lineWidthHidden.style.display === "block") {
        lineWidthHidden.style.display = "none";
    }
    if (penColorHidden.style.display === "none") {
        penColorHidden.style.display = "flex";
    } else {
        penColorHidden.style.display = "none";
    }
}
penColorHidden.onmouseleave = function () {
    clearTimeout(timer)
    timer = setTimeout(() => {
        penColorHidden.onmouseleave = function () {
            penColorHidden.style.display = "none";
        }
    }, 1000)
}
document.querySelector('.penColorItem:first-child').onclick = function () {
    penColor = 'black'
    penColor2 = penColor
}
document.querySelector('.penColorItem:nth-child(2)').onclick = function () {
    penColor = 'red'
    penColor2 = penColor
}
document.querySelector('.penColorItem:nth-child(3)').onclick = function () {
    penColor = 'blue'
    penColor2 = penColor
}
document.querySelector('.penColorItem:nth-child(4)').onclick = function () {
    penColor = 'orange'
    penColor2 = penColor
}
document.querySelector('.penColorItem:nth-child(5)').onclick = function () {
    penColor = 'green'
    penColor2 = penColor
}
document.querySelector('.penColorItem:nth-child(6)').onclick = function () {
    penColor = 'gray'
    penColor2 = penColor
}

let penLineWidth = penWidth;
shapeLineWidth.onclick = function () {
    if (penColorHidden.style.display === "flex") {
        penColorHidden.style.display = "none";
    }

    if (lineWidthHidden.style.display === "none") {
        lineWidthHidden.style.display = "block";
    } else {
        lineWidthHidden.style.display = "none";
    }
}
lineWidthHidden.onmouseleave = function () {
    clearTimeout(timer)
    timer = setTimeout(() => {
        lineWidthHidden.onmouseleave = function () {
            lineWidthHidden.style.display = "none";
        }
    }, 1000)
}
document.querySelector("#lineWidth-hidden>div:first-child").onclick = function () {
    penLineWidth = 1;
    penWidth = penLineWidth;
}
document.querySelector("#lineWidth-hidden>div:nth-child(2)").onclick = function () {
    penLineWidth = 2;
    penWidth = penLineWidth;
}
document.querySelector("#lineWidth-hidden>div:nth-child(3)").onclick = function () {
    penLineWidth = 4;
    penWidth = penLineWidth;
}
document.querySelector("#lineWidth-hidden>div:nth-child(4)").onclick = function () {
    penLineWidth = 8;
    penWidth = penLineWidth;
}
document.querySelector("#lineWidth-hidden>div:nth-child(5)").onclick = function () {
    penLineWidth = document.querySelector("#lineWidth-hidden>div:nth-child(5)>input").value;
    penWidth = penLineWidth;
}

function getUuid() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
        let r = (Math.random() * 16) | 0,
            v = c === 'x' ? r : (r & 0x3) | 0x8;
        return v.toString(16);
    });
}

function pcDrawLine() {
    canvas.onmousedown = function (e) {
        if (penColorHidden.style.display === "flex") {
            penColorHidden.style.display = "none";
        }
        if (lineWidthHidden.style.display === "block") {
            lineWidthHidden.style.display = "none";
        }
        if (eraserState) {
            penColor = "white"
            penLineWidth = 31
        }
        if (historyData.length === 20) {
            historyData.shift();
        }
        historyData.push(ctx.getImageData(0, 0, canvas.width, canvas.height));
        const mouseX = e.pageX - this.offsetLeft;
        const mouseY = e.pageY - this.offsetTop;
        flag = true;
        ctx.beginPath();
        ctx.lineWidth = penLineWidth;
        ctx.strokeStyle = penColor;
        ctx.moveTo((mouseX), (mouseY));
    };

    canvas.onmousemove = function (e) {
        let mouseX = e.pageX - this.offsetLeft;
        let mouseY = e.pageY - this.offsetTop;
        if (flag) {
            ctx.lineTo((mouseX), (mouseY));
            ctx.stroke();
        }
    }

    canvas.onmouseup = function (e) {
        flag = false;
        const data = canvas.toDataURL();
        const compressed = pako.deflate(data);
        if (socket != null) {
            socket.send(compressed);
        }
    }

    canvas.onmouseleave = function (e) {
        flag = false;
    }
}

pcDrawLine();

eraser.onclick = function () {
    if (penColorHidden.style.display === "flex") {
        penColorHidden.style.display = "none";
    }
    if (lineWidthHidden.style.display === "block") {
        lineWidthHidden.style.display = "none";
    }
    eraserState = true;
    eraser.classList.add('active');
    pen.classList.remove('active');
    penLineWidth = 31
    penColor = 'white'
};

pen.onclick = function () {
    if (penColorHidden.style.display === "flex") {
        penColorHidden.style.display = "none";
    }
    if (lineWidthHidden.style.display === "block") {
        lineWidthHidden.style.display = "none";
    }
    eraserState = false;
    pen.classList.add('active');
    eraser.classList.remove('active');
    penLineWidth = penWidth
    penColor = penColor2
};

save.onclick = function () {
    if (penColorHidden.style.display === "flex") {
        penColorHidden.style.display = "none";
    }
    if (lineWidthHidden.style.display === "block") {
        lineWidthHidden.style.display = "none";
    }
    let imgUrl = canvas.toDataURL("image/png");
    let saveA = document.createElement("a");
    document.body.appendChild(saveA);
    saveA.href = imgUrl;
    saveA.download = "pic" + (new Date).getTime();
    saveA.target = "_blank";
    saveA.click();
};

socket.onopen = function (event) {
    console.log("WebSocket connection opened.");
};

socket.onmessage = function (event) {
    const data = event.data;

    if (typeof data === 'string') {
        console.log('Received type 1 message');
        const message = JSON.parse(data);
        console.log('Received message:', message);
        if (message.type === 1) {
            dot.innerText = message.message;
        }
    } else {
        console.log('Received data message');
        const reader = new FileReader();

        reader.onload = function (event) {
            const arrayBuffer = event.target.result;
            const uint8Array = new Uint8Array(arrayBuffer);
            const decompressedData = pako.inflate(uint8Array, {to: 'string'});
            console.log("Received message decompressed:", decompressedData.length);
            const newImage = new Image();
            newImage.src = decompressedData;
            newImage.onload = function () {
                ctx.drawImage(newImage, 0, 0);
            };
        };

        reader.readAsArrayBuffer(data);
    }
};

socket.onclose = function (event) {
    console.log("WebSocket connection closed.");
    dot.style.backgroundColor = 'red';
    dot.style.color = 'red';
};

socket.onerror = function (error) {
    console.error("WebSocket error:", error);
    dot.style.backgroundColor = 'red';
    dot.style.color = 'red';
};