package zio.kafka.consumer.internal

import zio.Chunk

private[internal] final case class State(
  pendingRequests: Chunk[RunloopCommand.Request],
  pendingCommits: Chunk[RunloopCommand.Commit],
  assignedStreams: Chunk[PartitionStreamControl],
  subscriptionState: SubscriptionState
) {
  def addCommit(c: RunloopCommand.Commit): State   = copy(pendingCommits = pendingCommits :+ c)
  def addRequest(r: RunloopCommand.Request): State = copy(pendingRequests = pendingRequests :+ r)

  def shouldPoll: Boolean =
    subscriptionState.isSubscribed && (pendingRequests.nonEmpty || pendingCommits.nonEmpty || assignedStreams.isEmpty)
}

private[internal] object State {
  val initial: State = State(
    pendingRequests = Chunk.empty,
    pendingCommits = Chunk.empty,
    assignedStreams = Chunk.empty,
    subscriptionState = SubscriptionState.NotSubscribed
  )
}
