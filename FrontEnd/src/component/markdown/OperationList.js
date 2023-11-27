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

    getString() {
        let str = "";
        for (const operation of this.operations) {
            str += operation.content;
        }
        return str;
    }

    getSortedOperations() {
        return [...this.operations];
    }
}