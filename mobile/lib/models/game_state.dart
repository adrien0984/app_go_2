class GameState {
  final String gameId;
  final int boardSize;
  final String status;
  final String nextPlayer;
  final int moveCount;
  final DateTime createdAt;
  final List<List<String>> board;

  GameState({
    required this.gameId,
    required this.boardSize,
    required this.status,
    required this.nextPlayer,
    required this.moveCount,
    required this.createdAt,
    required this.board,
  });

  factory GameState.fromJson(Map<String, dynamic> json) {
    return GameState(
      gameId: json['gameId'],
      boardSize: json['boardSize'],
      status: json['status'],
      nextPlayer: json['nextPlayer'],
      moveCount: json['moveCount'],
      createdAt: DateTime.parse(json['createdAt']),
      board: List<List<String>>.from(
        (json['board'] as List).map(
          (row) => List<String>.from(row),
        ),
      ),
    );
  }
}
