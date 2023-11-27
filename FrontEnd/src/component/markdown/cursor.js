export function getCursorPositionInDivElement(divElement) {
    const selection = window.getSelection();
    const anchorNode = selection.anchorNode;
    if (anchorNode && divElement.contains(anchorNode)) {
        const textContent = divElement.textContent;
        const anchorOffset = selection.anchorOffset;
        let cursorIndex = 0;
        for (let i = 0; i < textContent.length; i++) {
            if (anchorNode === divElement && anchorOffset === i) {
                break;
            }

            const node = divElement.childNodes[i];
            const nodeTextLength = node.textContent.length;

            if (anchorNode === node) {
                cursorIndex += anchorOffset;
                break;
            }
            cursorIndex += nodeTextLength;
        }
        return cursorIndex;
    }
    return -1;
}

export function getCursorPos(text) {
    const selection = window.getSelection();
    let cursorElementText = '';
    let position = 0;

    if (selection.rangeCount > 0) {
        const range = selection.getRangeAt(0);
        const cursorElement = range.startContainer.parentElement;
        cursorElementText = cursorElement.innerText;
        if (cursorElement.classList.contains('cm-line')) {
            position = getCursorPositionInDivElement(cursorElement);
        }
    }
    let lines = text.split('\n');
    let count = 0;
    lines.some((line) => {
        if (line === cursorElementText) {
            return true;
        } else {
            count += line.length + 1;
        }
        return false;
    });

    return count + position;
}

export function updateCursorPosition(oldText, newText, cursorPosition) {
    if (cursorPosition === undefined) {
        return 0;
    } else if (newText.length > oldText.length) {
        let addPos = findAddedTextPositions(oldText, newText);
        let start = addPos[0];
        let end = addPos[1];
        if (end < cursorPosition) {
            console.log(1);
            return Math.min(cursorPosition + end - start, newText.length - 1);
        } else {
            return cursorPosition;
        }
    } else if (newText.length < oldText.length) {
        let delPos = findDeletedTextPositions(oldText, newText);
        let start = delPos[0];
        let end = delPos[1];
        console.info('start:', start, 'end:', end, 'cursorPosition:', cursorPosition);
        if (start >= cursorPosition) {
            return Math.min(cursorPosition, newText.length);
        } else if (end < cursorPosition) {
            return Math.max(cursorPosition - (end - start + 1), 0);
        } else {
            return Math.max(0, start);
        }
    } else {
        return cursorPosition;
    }
}

export function findDeletedTextPositions(oldText, newText) {
    let deletedPositions = [oldText.length];

    for (let i = 0; i < newText.length; i++) {
        if (oldText[i] !== newText[i]) {
            deletedPositions[0] = i;
            break;
        }
    }
    deletedPositions[1] = deletedPositions[0] + oldText.length - newText.length - 1;

    return deletedPositions;
}

export function findAddedTextPositions(oldText, newText) {
    let addedPositions = [];

    for (let i = 0; i < oldText.length; i++) {
        if (oldText[i] !== newText[i]) {
            addedPositions[0] = i;
            break;
        }
    }
    addedPositions[1] = addedPositions[0] + newText.length - oldText.length;

    return addedPositions;
}