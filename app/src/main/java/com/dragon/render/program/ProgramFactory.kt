package com.dragon.render.program

enum class ProgramKey{
    OES,
    TEXTURE
}
class ProgramFactory {
    companion object{
        fun create(key: ProgramKey) = when(key){
            ProgramKey.OES -> OesTextureProgram()
            ProgramKey.TEXTURE -> TextureProgram()
        }
    }
}