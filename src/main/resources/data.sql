
   INSERT IGNORE INTO post_attribute_definition (code, label, input_type, is_required, is_used_in_search, search_weight,
    sort_order, is_displayed_on_post_view) VALUES ('title', 'Title', 'text', 1, 1, 10, 0, 1);

    INSERT IGNORE INTO post_attribute_definition (code, label, input_type, accept, sort_order, is_displayed_on_post_view)
    VALUES ('image', 'Image', 'file', 'image/*', 1, 1);

    INSERT IGNORE INTO post_attribute_definition (code, label, input_type, is_used_in_search, is_wysiwyg_enabled,
     sort_order, is_displayed_on_post_view) VALUES ('description', 'Description', 'textarea', 1, 1, 2, 1);


