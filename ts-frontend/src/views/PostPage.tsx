import { useParams } from "react-router-dom";
import { getByUrlKey } from "../actions/PostActions";
import { useEffect, useState } from "react";
import Post from "../models/Post";
import he from "he";
import DOMPurify from "isomorphic-dompurify";

export const PostPage: React.FC = () => {
  const { urlKey } = useParams();
  const [post, setPost] = useState<Post>();

  const options: Intl.DateTimeFormatOptions = {
    year: "numeric",
    month: "long",
    day: "numeric",
  };

  useEffect(() => {
    const fetchCurrentPost = async () => {
      const postReq = await getByUrlKey(urlKey!);
      setPost(postReq);
    };
    fetchCurrentPost();
  }, [urlKey]);

  return (
    <>
      {}
      <h1 className="text-center m-2">{post?.title}</h1>
      <div className="date-container  mx-5 p-2">
        <div className="date-create">
          <i>Posted on: </i>
          {new Date(post?.createdAt!).toLocaleDateString(undefined, options)}
        </div>
        <div className="date-update">
          <i>Last update: </i>
          {new Date(post?.updatedAt!).toLocaleDateString(undefined, options)}
        </div>
      </div>

      <div className="post-img-container d-flex align-items-center justify-content-center">
        <img
          className="post-img img-fluid col-8 sm-12"
          src={`${post?.image}`}
          alt={`${post?.title}`}
          title={`${post?.title}`}
          max-width="100%"
        />
      </div>
      <div
        className="post-content p-3"
        dangerouslySetInnerHTML={{
          __html: DOMPurify.sanitize(
            he.decode(post?.description ? post?.description : ""),
            { USE_PROFILES: { html: true } }
          ),
        }}
      />
    </>
  );
};
