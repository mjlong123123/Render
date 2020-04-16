package com.dragon.render.program

enum class ProgramKey{
    PRIMITIVE,
    OES,
    TEXTURE
}
class ProgramFactory {
    companion object{
        fun create(key: ProgramKey) = when(key){
            ProgramKey.PRIMITIVE->PrimitiveProgram()
            ProgramKey.OES -> OesTextureProgram()
            ProgramKey.TEXTURE -> TextureProgram()
        }
    }
}