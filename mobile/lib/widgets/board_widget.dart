import 'package:flutter/material.dart';

class BoardWidget extends StatelessWidget {
  final List<List<String>> board;
  final int boardSize;
  final Function(int, int) onCellTapped;
  final bool enabled;

  const BoardWidget({
    Key? key,
    required this.board,
    required this.boardSize,
    required this.onCellTapped,
    this.enabled = true,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final cellSize = (MediaQuery.of(context).size.width - 32) / boardSize;

    return Container(
      padding: const EdgeInsets.all(16),
      child: Column(
        children: [
          for (int row = 0; row < boardSize; row++)
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                for (int col = 0; col < boardSize; col++)
                  GestureDetector(
                    onTap: enabled ? () => onCellTapped(row, col) : null,
                    child: Container(
                      width: cellSize,
                      height: cellSize,
                      decoration: BoxDecoration(
                        border: Border(
                          top: BorderSide(
                            color: Colors.black26,
                            width: 0.5,
                          ),
                          left: BorderSide(
                            color: Colors.black26,
                            width: 0.5,
                          ),
                        ),
                      ),
                      child: Center(
                        child: _buildStone(board[row][col], cellSize * 0.4),
                      ),
                    ),
                  ),
              ],
            ),
        ],
      ),
    );
  }

  Widget _buildStone(String cellState, double stoneRadius) {
    if (cellState == 'EMPTY') {
      return const SizedBox.shrink();
    }

    final color = cellState == 'BLACK' ? Colors.black : Colors.white;
    return Container(
      width: stoneRadius * 2,
      height: stoneRadius * 2,
      decoration: BoxDecoration(
        color: color,
        shape: BoxShape.circle,
        boxShadow: [
          BoxShadow(
            color: Colors.black26,
            blurRadius: 2,
            offset: const Offset(1, 1),
          ),
        ],
      ),
    );
  }
}
