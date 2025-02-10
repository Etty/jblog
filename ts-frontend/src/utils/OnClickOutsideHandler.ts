class OnClickOutsideHandler {

    constructor(private localNode: Element, private callback: { (event: Event): void }) {
        if (!callback) throw new Error("Missing callback");
        document.addEventListener("mousedown", this.eventHandler);
        document.addEventListener("touchstart", this.eventHandler);
    }

    private eventHandler = (event: Event) => {

        var source: Node = <any>event.target;
        var found = false;

        // If source=local then this event came from "somewhere"
        // inside and should be ignored. We could handle this with
        // a layered approach, too, but that requires going back to
        // thinking in terms of Dom node nesting, running counter
        // to React's "you shouldn't care about the DOM" philosophy.
        while (source.parentNode) {
            found = (source === this.localNode);
            if (found) return;
            source = source.parentNode;
        }

        this.callback(event);
    }

    dispose() {
        document.removeEventListener("mousedown", this.eventHandler);
        document.removeEventListener("touchstart", this.eventHandler);
    }
}

export default OnClickOutsideHandler;