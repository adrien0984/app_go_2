import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/game_state.dart';
import '../services/game_service.dart';
import '../services/auth_service.dart';
import '../widgets/board_widget.dart';

class GameScreen extends StatefulWidget {
  final String gameId;

  const GameScreen({Key? key, required this.gameId}) : super(key: key);

  @override
  State<GameScreen> createState() => _GameScreenState();
}

class _GameScreenState extends State<GameScreen> {
  GameState? _gameState;
  String? _errorMessage;
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadGame();
  }

  void _loadGame() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final gameService = _createGameService();
      final game = await gameService.getGame(widget.gameId);
      setState(() {
        _gameState = game;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _errorMessage = 'Erreur: ${e.toString()}';
        _isLoading = false;
      });
    }
  }

  void _playMove(int row, int col) async {
    if (_gameState == null) return;

    setState(() {
      _errorMessage = null;
    });

    try {
      final gameService = _createGameService();
      final updatedGame = await gameService.playMove(widget.gameId, row, col);
      setState(() {
        _gameState = updatedGame;
      });
    } catch (e) {
      setState(() {
        _errorMessage = 'Coup illégal: ${e.toString()}';
      });
    }
  }

  void _passMove() async {
    if (_gameState == null) return;

    setState(() {
      _errorMessage = null;
    });

    try {
      final gameService = _createGameService();
      final updatedGame = await gameService.passMove(widget.gameId);
      setState(() {
        _gameState = updatedGame;
      });
    } catch (e) {
      setState(() {
        _errorMessage = 'Erreur lors du passage: ${e.toString()}';
      });
    }
  }

  GameService _createGameService() {
    final authService = Provider.of<AuthService>(context, listen: false);
    return GameService(
      baseUrl: 'http://localhost:8080',
      getToken: () => authService.accessToken ?? '',
    );
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return Scaffold(
        appBar: AppBar(title: const Text('Partie de Go')),
        body: const Center(child: CircularProgressIndicator()),
      );
    }

    if (_gameState == null) {
      return Scaffold(
        appBar: AppBar(title: const Text('Erreur')),
        body: Center(
          child: Text(_errorMessage ?? 'Impossible de charger la partie'),
        ),
      );
    }

    final game = _gameState!;
    final isGameFinished = game.status == 'FINISHED';

    return Scaffold(
      appBar: AppBar(
        title: const Text('Partie de Go'),
      ),
      body: SingleChildScrollView(
        child: Column(
          children: [
            // Game info
            Container(
              padding: const EdgeInsets.all(16),
              color: Colors.grey[100],
              child: Column(
                children: [
                  Text(
                    'Plateau: ${game.boardSize}x${game.boardSize}',
                    style: const TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    'Joueur: ${game.nextPlayer == 'BLACK' ? 'Noir' : 'Blanc'}',
                    style: const TextStyle(fontSize: 14),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    'Coups: ${game.moveCount}',
                    style: const TextStyle(fontSize: 14),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    'État: ${isGameFinished ? 'Terminée' : 'En cours'}',
                    style: TextStyle(
                      fontSize: 14,
                      color:
                          isGameFinished ? Colors.red : Colors.green,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ],
              ),
            ),
            // Board
            BoardWidget(
              board: game.board,
              boardSize: game.boardSize,
              onCellTapped: _playMove,
              enabled: !isGameFinished,
            ),
            // Error message
            if (_errorMessage != null)
              Container(
                padding: const EdgeInsets.all(16),
                color: Colors.red[100],
                child: Row(
                  children: [
                    Icon(Icons.error, color: Colors.red[700]),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        _errorMessage!,
                        style: TextStyle(color: Colors.red[700]),
                      ),
                    ),
                  ],
                ),
              ),
            // Action buttons
            Padding(
              padding: const EdgeInsets.all(16),
              child: Row(
                children: [
                  Expanded(
                    child: ElevatedButton.icon(
                      onPressed: isGameFinished ? null : _passMove,
                      icon: const Icon(Icons.skip_next),
                      label: const Text('Passer'),
                    ),
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: ElevatedButton.icon(
                      onPressed: _loadGame,
                      icon: const Icon(Icons.refresh),
                      label: const Text('Actualiser'),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
