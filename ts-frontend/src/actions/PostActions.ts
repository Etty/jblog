import axios from 'axios';
import Post from '../models/Post';

export async function getByUrlKey(urlKey: string): Promise<Post> {
    const {data} = await axios.get(`${process.env.REACT_APP_API}post/${urlKey}`);
    return data;
}