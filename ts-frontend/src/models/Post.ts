class Post {
    private _postIdentifier!:string;
    private _title!: string;
    private _image?: string;
    private _description?:string;
    private _urlKey!: string;
    private _createdAt!: string;
    private _updatedAt!: string;

    public get postIdentifier(): string {
        return this._postIdentifier;
    }

    public set postIdentifier(value: string) {
        this._postIdentifier = value;
    }

    public get title(): string {
        return this._title;
    }

    public set title(value: string) {
        this._title = value;
    }

    public get image(): string {
        return this._image ? this._image : "";
    }

    public set image(value: string) {
        this._image = value;
    }

    public get description(): string {
        return this._description ? this._description : "";
    }

    public set description(value: string) {
        this._description = value;
    }

    public get urlKey(): string {
        return this._urlKey;
    }

    public set urlKey(value: string) {
        this._urlKey = value;
    }

    public get createdAt(): string {
        return this._createdAt;
    }

    public set createdAt(value: string) {
        this._createdAt = value;
    }

    public get updatedAt(): string {
        return this._updatedAt;
    }

    public set updatedAt(value: string) {
        this._updatedAt = value;
    }
}

export default Post;