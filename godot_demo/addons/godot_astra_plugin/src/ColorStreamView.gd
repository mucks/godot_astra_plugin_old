extends Sprite

func _on_AstraController_new_color_frame(width, height, colors):
    var img = Image.new()
    img.create_from_data(width, height, false, Image.FORMAT_RGBA8, colors)

    var imageTexture = ImageTexture.new()
    imageTexture.create_from_image(img, 7)

    set_texture(imageTexture);