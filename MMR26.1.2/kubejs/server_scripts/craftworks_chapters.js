// priority: 0

// Locks Craftworks recipes behind Chapters stages (ADR 0007).
CraftworksEvents.lock(event => {
  if (!Platform.isLoaded('chapters')) return
  const player = event.entity
  if (!player) return
  const StageManager = Java.loadClass('com.gabinx.chapters.stage.StageManager')
  const ChaptersAPI = Java.loadClass('com.gabinx.chapters.api.ChaptersAPI')
  if (StageManager.get().book().isRecipeLocked(event.recipe, ChaptersAPI.getStages(player))) {
    event.lock()
  }
})
