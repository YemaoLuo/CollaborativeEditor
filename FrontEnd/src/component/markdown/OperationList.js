export class OperationList {
    constructor() {
        this.operations = [];
    }

    add(operation) {
        this.operations.push(operation);
        this.operations.sort((a, b) => a.timestamp - b.timestamp);
    }

    remove(operation) {
        const index = this.operations.indexOf(operation);
        if (index !== -1) {
            this.operations.splice(index, 1);
        }
    }

    reset(newOperations) {
        this.operations = newOperations;
        this.operations.sort((a, b) => a.timestamp - b.timestamp);
    }

    getLocalTimestamp() {
        return this.operations[this.operations.length - 1].timestamp;
    }

    getString() {
        let str = "";
        for (const operation of this.operations) {
            if (operation.type === "insert") {
                str = str.slice(0, operation.position) + operation.content + str.slice(operation.position);
            } else if (operation.type === "delete") {
                str = str.slice(0, operation.position) + str.slice(operation.position + operation.content.length);
            }
        }
        return str;
    }

    getSortedOperations() {
        return [...this.operations];
    }
}