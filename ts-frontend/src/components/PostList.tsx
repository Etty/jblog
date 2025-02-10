import React from "react";
import Post from "../models/Post";
import he from "he";
import DOMPurify from "isomorphic-dompurify";

interface Props {
  postList: Post[];
}

export const PostList: React.FC<Props> = ({ postList }) => {
  return (
    <>
      <div className="container post-list text-center">
        {postList.map((post, idx) => (
          <div className="post-item border-bottom" key={idx}>
            <a className="post-item-link" href={`/${post.urlKey}`}>
              <h2 className="post-title">{post.title}</h2>
              <div className="post-img">
                <img
                  className="post-img img-fluid col-5 sm-7"
                  src={`${post?.image}`}
                  alt={`${post?.title}`}
                  title={`${post?.title}`}
                  max-width="100%"
                />
              </div>
              <div
                className="post-content p-3"
                dangerouslySetInnerHTML={{
                  __html:
                    DOMPurify.sanitize(
                      he.decode(post?.description ? post?.description : ""),
                      { USE_PROFILES: { html: true } }
                    ).substring(0, 100) + "...",
                }}
              />
            </a>
          </div>
        ))}
      </div>
    </>
  );
};
