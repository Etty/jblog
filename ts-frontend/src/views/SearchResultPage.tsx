import { useEffect, useState } from "react";
import Pagination from "../components/Pagination";
import { search } from "../actions/SearchAction";
import SearchResult from "../models/SearchResult";
import { useParams } from "react-router-dom";
import { PostList } from "../components/PostList";

export const SearchResultPage: React.FC = () => {
  const [currentPage, setCurrentPage] = useState(1);
  const [lastPage, setLastPage] = useState(1);
  const [searchResult, setSearchResult] = useState<SearchResult>();
  const [isLoading, setIsLoading] = useState(true);

  const { query } = useParams();

  useEffect(() => {
    const fetchCurrentPage = async () => {
      const sr: SearchResult = await search(query!, currentPage - 1);
      setSearchResult(sr);
      setLastPage(
        Math.floor(sr!.resultsCount! / 10) + (sr!.resultsCount! % 10 ? 1 : 0)
      );
      window.scrollTo(0, 0);
      setIsLoading(false);
    };
    fetchCurrentPage().catch((error: any) => {
      setIsLoading(false);
    });
  }, [currentPage]);

  if (isLoading) {
    return <div>Loading...</div>;
  }

  if (searchResult?.resultsCount === 0) {
    return (
      <div className="alert alert-warning mt-3">
        No search results found for "{query}"
      </div>
    );
  }

  return (
    <>
      <div className="alert alert-success mt-3">
        Showing results for "
        {decodeURIComponent((query + "").replace(/\+/g, "%20"))}"
      </div>
      <div className="justify-content-end">
        Viewing items {1 + 10 * (currentPage - 1)}-
        {currentPage !== lastPage
          ? 1 + 10 * (currentPage - 1) + 9
          : searchResult?.resultsCount}{" "}
        of {searchResult?.resultsCount}
      </div>
      <PostList postList={searchResult!.resultItems} />
      {lastPage > 1 ? (
        <Pagination
          currentPage={currentPage}
          lastPage={lastPage}
          maxLength={7}
          setCurrentPage={setCurrentPage}
        />
      ) : (
        <></>
      )}
    </>
  );
};
