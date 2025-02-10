import Post from "./Post";

class SearchResult {
    constructor(
        public resultItems: Post[], 
        public highlights: Record<string, Record<string, string>>,
         public searchResultsLink: string, 
         public resultsCount: number) {

    }
   //  public get getResultItems(): Post[] {
   //      return this.resultItems;
   //   }

   //   public getHighlights(): string[] {
   //      return this._highlights;
   //   }

   //   public getSearchResultsLink(): string {
   //      return this._searchResultsLink;
   //   }

   //   public getResultsCount(): number {
   //      return this._resultsCount;
   //   }
}

export default SearchResult;