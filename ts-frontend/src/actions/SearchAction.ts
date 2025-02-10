import axios from 'axios';
import SearchResult from '../models/SearchResult';

  export async function search(query: string, pageNum: number = 0): Promise<SearchResult> {
    // let pg = pageNum !== undefined ? pageNum : 0;
    const { data } = await axios.get(`${process.env.REACT_APP_API}search/${query}/${pageNum}`);
    return data;
}