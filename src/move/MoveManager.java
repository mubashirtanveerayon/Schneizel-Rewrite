package move;

import board.ChessBoard;
import util.Constants;
import util.FenUtils;
import util.GameState;
import util.Util;

import java.util.ArrayList;
import java.util.HashMap;

public class MoveManager {

    ChessBoard cb;
    ArrayList<String> moves;


    public MoveManager(ChessBoard cb){
        this.cb = cb;
        moves = new ArrayList<>();
    }

    public String cvt(String moveStr){
        String stdMove;
        if(moveStr.contains(Constants.KING_SIDE_CASTLING)){
            int rank = cb.turn == Constants.WHITE?7:0;
            stdMove = Util.cvtMove(4,rank,5,rank);
        }else if(moveStr.contains(Constants.QUEEN_SIDE_CASTLING)){
            int rank = cb.turn == Constants.WHITE?7:0;
            stdMove = Util.cvtMove(4,rank,2,rank);
        }else{
            stdMove = Util.cvtMove(Integer.parseInt(Character.toString(moveStr.charAt(0))),Integer.parseInt(Character.toString(moveStr.charAt(1))),Integer.parseInt(Character.toString(moveStr.charAt(2))),Integer.parseInt(Character.toString(moveStr.charAt(3))));
        }
        return stdMove;
    }

    public String parse(String stdMove){
        int lf = Constants.FILES.indexOf(stdMove.charAt(0));
        int lr = Constants.RANKS.indexOf(stdMove.charAt(1));
        int df = Constants.FILES.indexOf(stdMove.charAt(2));
        int dr = Constants.RANKS.indexOf(stdMove.charAt(3));
        String move=Util.cvtMove(lf,lr,df,dr,cb.board,cb.fenParts)+Constants.MOVE_SEPARATOR;
        switch(Character.toUpperCase(cb.board[lr][lf])){
            case Constants.WHITE_KING:
                switch(lf-df) {
                    case 2:
                        move = Constants.QUEEN_SIDE_CASTLING;
                        break;
                    case -2:
                        move = Constants.KING_SIDE_CASTLING;
                        break;
                }
                move+=Constants.MOVE_SEPARATOR+cb.fenParts[9]+Constants.MOVE_SEPARATOR+cb.fenParts[10];
                break;
            case Constants.WHITE_PAWN:
                if(dr == 0){
                    move+=Character.toUpperCase(stdMove.charAt(4));
                }else if(dr == 7){
                    move+=Character.toLowerCase(stdMove.charAt(4));
                }else if(lf-df!=0&&cb.board[dr][df] == Constants.EMPTY_SQUARE){
                    move+=Constants.EN_PASSANT_NOTATION;
                }

                break;
            default:
                move = move.substring(0,move.length()-1);
        }
        return move;
    }


    public void moveGenerationTest(int depth,boolean stdOutput){
        ArrayList<String> moves = getAllMoves() ;
        int numPositions = 0;
        String fen = FenUtils.cat(cb.fenParts);
        for (String moveStr:moves) {
            makeMove(moveStr);
            int numMoves = recurseMoveGeneration(depth-1);
            numPositions += numMoves;
            undoMove(moveStr);
            if(!fen.equals(FenUtils.cat(cb.fenParts))){
                System.out.println(fen);
                System.out.println(FenUtils.cat(cb.fenParts));
                System.out.println(cvt(moveStr));
            }
            if(stdOutput){
                String stdMove;
                if(moveStr.contains(Constants.KING_SIDE_CASTLING)){
                    int rank = cb.turn == Constants.WHITE?7:0;
                    stdMove = Util.cvtMove(4,rank,5,rank);
                }else if(moveStr.contains(Constants.QUEEN_SIDE_CASTLING)){
                    int rank = cb.turn == Constants.WHITE?7:0;
                    stdMove = Util.cvtMove(4,rank,2,rank);
                }else{
                    stdMove = Util.cvtMove(Integer.parseInt(Character.toString(moveStr.charAt(0))),Integer.parseInt(Character.toString(moveStr.charAt(1))),Integer.parseInt(Character.toString(moveStr.charAt(2))),Integer.parseInt(Character.toString(moveStr.charAt(3))));
                }
                System.out.println(stdMove + ": " + numMoves);
            }else {
                System.out.println(moveStr + ": " + numMoves);
            }
        }
        System.out.println("Total number of moves for depth "+depth+" : "+numPositions);

    }

    private int recurseMoveGeneration(int depth){
        if (depth <= 0) {
            return 1;
        }
        ArrayList<String> moves = getAllMoves() ;
        int numPositions = 0;
        String fen = FenUtils.cat(cb.fenParts);
        for (String moveStr:moves) {

            makeMove(moveStr);
            numPositions += recurseMoveGeneration(depth-1);
            undoMove(moveStr);
            if(!fen.equals(FenUtils.cat(cb.fenParts))){
                System.out.println(cvt(moveStr));
            }
        }
        return numPositions;
    }


    public ArrayList<String> getAllMoves() {
        ArrayList<String> allMoves = new ArrayList<>();
        for(int i=0;i<8;i++){
            for(int j=0;j<8;j++){
                if(cb.board[i][j] != Constants.EMPTY_SQUARE && !Util.isEnemyPiece(cb.turn,cb.board[i][j])){
                    allMoves.addAll(generateMove(j,i));
                }
            }
        }
        return allMoves;
    }

    // Here are all the functions from previous move class
    public void makeMove(String move){
        if(move.contains(Constants.KING_SIDE_CASTLING)){
            int rank = cb.turn == Constants.WHITE?7:0;
            cb.board[rank][6] = cb.board[rank][4];
            cb.board[rank][4] = Constants.EMPTY_SQUARE;
            cb.board[rank][5] = cb.board[rank][7];
            cb.board[rank][7] = Constants.EMPTY_SQUARE;
            cb.pieceLocations.add(6 + rank * 8);
            cb.pieceLocations.remove((Object)(4 + rank * 8));
            cb.pieceLocations.add(5 + rank * 8);
            cb.pieceLocations.remove((Object)(7 + rank * 8));
            if(cb.turn == Constants.WHITE){
                cb.whiteKingPosition[0] = 6;
                cb.whiteKingPosition[1] = 7;
            }else{
                cb.blackKingPosition[0] = 6;
                cb.blackKingPosition[1] = 0;
            }
            cb.fenParts[rank] = FenUtils.getRank(cb.board[rank]);
            if(cb.turn  == Constants.WHITE){
                cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.WHITE_KING),"").replace(Character.toString(Constants.WHITE_QUEEN),"");
            }else{
                cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.BLACK_KING),"").replace(Character.toString(Constants.BLACK_QUEEN),"");
            }
            cb.turn  = cb.turn  == Constants.WHITE?Constants.BLACK:Constants.WHITE;
            cb.fenParts[8] = Character.toString(cb.turn);
            cb.fenParts[10] = "-";
        }else if(move.contains(Constants.QUEEN_SIDE_CASTLING)){
            int rank = cb.turn == Constants.WHITE?7:0;
            cb.board[rank][2] = cb.board[rank][4];
            cb.board[rank][4] = Constants.EMPTY_SQUARE;
            cb.board[rank][3] = cb.board[rank][0];
            cb.board[rank][0] = Constants.EMPTY_SQUARE;
            cb.pieceLocations.add(2 + rank * 8);
            cb.pieceLocations.remove((Object)(4 + rank * 8));
            cb.pieceLocations.add(3 + rank * 8);
            cb.pieceLocations.remove((Object)(0 + rank * 8));
            if(cb.turn == Constants.WHITE){
                cb.whiteKingPosition[0] = 2;
                cb.whiteKingPosition[1] = 7;
            }else{
                cb.blackKingPosition[0] = 2;
                cb.blackKingPosition[1] = 0;
            }
            cb.fenParts[rank] = FenUtils.getRank(cb.board[rank]);
            if(cb.turn  == Constants.WHITE){
                cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.WHITE_KING),"").replace(Character.toString(Constants.WHITE_QUEEN),"");
            }else{
                cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.BLACK_KING),"").replace(Character.toString(Constants.BLACK_QUEEN),"");
            }
            cb.turn  = cb.turn  == Constants.WHITE?Constants.BLACK:Constants.WHITE;
            cb.fenParts[8] = Character.toString(cb.turn);
            cb.fenParts[10] = "-";
        }else if(move.charAt(1) == move.charAt(3)){
            int rank = Integer.parseInt(String.valueOf(move.charAt(1)));
            int locFile = Integer.parseInt(String.valueOf(move.charAt(0)));
            int destFile = Integer.parseInt(String.valueOf(move.charAt(2)));
            if(cb.board[rank][locFile] == Constants.WHITE_KING){
                cb.whiteKingPosition[0] = destFile;
                cb.whiteKingPosition[1] = rank;
            }else if(cb.board[rank][locFile] == Constants.BLACK_KING){
                cb.blackKingPosition[0] = destFile;
                cb.blackKingPosition[1] = rank;
            }

            if(rank == 0 || rank == 7){
                switch(cb.board[rank][locFile]){
                    case Constants.WHITE_ROOK:
                        if(locFile==0 && cb.fenParts[9].contains(String.valueOf(Constants.WHITE_QUEEN))){
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.WHITE_QUEEN),"");
                        }else if(locFile==7 && cb.fenParts[9].contains(String.valueOf(Constants.WHITE_KING))){
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.WHITE_KING), "");
                        }
                        break;
                    case Constants.WHITE_KING:
                        if(cb.fenParts[9].contains(String.valueOf(Constants.WHITE_QUEEN)) || cb.fenParts[9].contains(String.valueOf(Constants.WHITE_KING))) {
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.WHITE_KING), "").replace(Character.toString(Constants.WHITE_QUEEN), "");
                        }
                        break;
                    case Constants.BLACK_ROOK:
                        if(locFile==0 && cb.fenParts[9].contains(String.valueOf(Constants.BLACK_QUEEN))){
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.BLACK_QUEEN),"");
                        }else if(locFile==7 && cb.fenParts[9].contains(String.valueOf(Constants.BLACK_KING))) {
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.BLACK_KING), "");
                        }
                        break;
                    case Constants.BLACK_KING:
                        if(cb.fenParts[9].contains(String.valueOf(Constants.BLACK_QUEEN)) || cb.fenParts[9].contains(String.valueOf(Constants.BLACK_KING))) {
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.BLACK_KING), "").replace(Character.toString(Constants.BLACK_QUEEN), "");
                        }
                        break;
                }
                switch(cb.board[rank][destFile]){
                    case Constants.WHITE_ROOK:
                        if(destFile==0 && cb.fenParts[9].contains(String.valueOf(Constants.WHITE_QUEEN))){
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.WHITE_QUEEN),"");
                        }else if(destFile==7 && cb.fenParts[9].contains(String.valueOf(Constants.WHITE_KING))){
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.WHITE_KING), "");
                        }
                        break;
                    case Constants.BLACK_ROOK:
                        if(destFile==0 && cb.fenParts[9].contains(String.valueOf(Constants.BLACK_QUEEN))){
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.BLACK_QUEEN),"");
                        }else if(destFile==7 && cb.fenParts[9].contains(String.valueOf(Constants.BLACK_KING))) {
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.BLACK_KING), "");
                        }
                        break;
                }
            }

            cb.board[rank][destFile] = cb.board[rank][locFile];
            cb.board[rank][locFile] = Constants.EMPTY_SQUARE;
            cb.pieceLocations.add(destFile + rank * 8);
            cb.pieceLocations.remove((Object)(locFile + rank * 8));

            cb.fenParts[rank] = FenUtils.getRank(cb.board[rank]);
            cb.fenParts[10] = "-";
            cb.turn  = cb.turn  == Constants.WHITE?Constants.BLACK:Constants.WHITE;
            cb.fenParts[8] = Character.toString(cb.turn);

        }else if(move.charAt(0) == move.charAt(2)){
            int file = Integer.parseInt(String.valueOf(move.charAt(0)));
            int locRank = Integer.parseInt(String.valueOf(move.charAt(1)));
            int destRank = Integer.parseInt(String.valueOf(move.charAt(3)));
            if(cb.board[locRank][file] == Constants.WHITE_KING){
                cb.whiteKingPosition[0] = file;
                cb.whiteKingPosition[1] = destRank;
            }else if(cb.board[locRank][file] == Constants.BLACK_KING){
                cb.blackKingPosition[0] = file;
                cb.blackKingPosition[1] = destRank;
            }

            if(locRank == 0 || locRank == 7){
                switch(cb.board[locRank][file]){
                    case Constants.BLACK_ROOK:{
                        if(file==0 && cb.fenParts[9].contains(String.valueOf(Constants.BLACK_QUEEN))){
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.BLACK_QUEEN),"");
                        }else if(file==7 && cb.fenParts[9].contains(String.valueOf(Constants.BLACK_KING))) {
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.BLACK_KING), "");
                        }
                        break;
                    }
                    case Constants.WHITE_ROOK:{
                        if(file==0 && cb.fenParts[9].contains(String.valueOf(Constants.WHITE_QUEEN))){
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.WHITE_QUEEN),"");
                        }else if(file==7 && cb.fenParts[9].contains(String.valueOf(Constants.WHITE_KING))) {
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.WHITE_KING), "");
                        }
                        break;
                    }
                    case Constants.BLACK_KING:{
                        if(cb.fenParts[9].contains(String.valueOf(Constants.BLACK_QUEEN)) || cb.fenParts[9].contains(String.valueOf(Constants.BLACK_KING))) {
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.BLACK_KING), "").replace(Character.toString(Constants.BLACK_QUEEN), "");
                        }
                        break;
                    }
                    case Constants.WHITE_KING:{
                        if(cb.fenParts[9].contains(String.valueOf(Constants.WHITE_QUEEN)) || cb.fenParts[9].contains(String.valueOf(Constants.WHITE_KING))) {
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.WHITE_KING), "").replace(Character.toString(Constants.WHITE_QUEEN), "");
                        }
                        break;
                    }
                }
            }
            if(destRank == 0 || destRank == 7){
                switch(cb.board[destRank][file]){
                    case Constants.WHITE_ROOK:
                        if(file==0 && cb.fenParts[9].contains(String.valueOf(Constants.WHITE_QUEEN))){
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.WHITE_QUEEN),"");
                        }else if(file==7 && cb.fenParts[9].contains(String.valueOf(Constants.WHITE_KING))){
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.WHITE_KING), "");
                        }
                        break;
                    case Constants.BLACK_ROOK:
                        if(file==0 && cb.fenParts[9].contains(String.valueOf(Constants.BLACK_QUEEN))){
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.BLACK_QUEEN),"");
                        }else if(file==7 && cb.fenParts[9].contains(String.valueOf(Constants.BLACK_KING))) {
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.BLACK_KING), "");
                        }
                        break;
                }

                if(Character.toUpperCase(cb.board[locRank][file]) == Constants.WHITE_PAWN){
                    cb.board[locRank][file] = move.split(Constants.MOVE_SEPARATOR)[4].charAt(0);
                }

            }

            if(Character.toUpperCase(cb.board[locRank][file]) == Constants.WHITE_PAWN) {
                //double pawn push -> creates en-passant square
                if (locRank == 1 && cb.board[locRank][file] == Constants.BLACK_PAWN && destRank == 3) {
                    cb.fenParts[10] = Constants.FILES.charAt(file) + "6";
                } else if (locRank == 6 && cb.board[locRank][file] == Constants.WHITE_PAWN && destRank == 4) {
                    cb.fenParts[10] = Constants.FILES.charAt(file) + "3";
                } else {
                    cb.fenParts[10] = "-";
                }
            }

            cb.board[destRank][file] = cb.board[locRank][file];
            cb.board[locRank][file] = Constants.EMPTY_SQUARE;
            cb.pieceLocations.add(file + destRank * 8);
            cb.pieceLocations.remove((Object)(file+locRank*8));
            cb.fenParts[locRank] = FenUtils.getRank(cb.board[locRank]);
            cb.fenParts[destRank] = FenUtils.getRank(cb.board[destRank]);
            cb.turn  = cb.turn  == Constants.WHITE?Constants.BLACK:Constants.WHITE;
            cb.fenParts[8] = Character.toString(cb.turn);

        }else if(move.contains(Constants.EN_PASSANT_NOTATION)){
            int locFile = Integer.parseInt(String.valueOf(move.charAt(0)));
            int destFile = Integer.parseInt(String.valueOf(move.charAt(2)));
            int locRank = Integer.parseInt(String.valueOf(move.charAt(1)));
            int destRank = Integer.parseInt(String.valueOf(move.charAt(3)));
            switch(cb.board[locRank][locFile]){
                case Constants.WHITE_PAWN:{
                    cb.board[locRank][destFile] = Constants.EMPTY_SQUARE;
                    cb.pieceLocations.remove((Object)(destFile+locRank*8));
                    break;
                }
                case Constants.BLACK_PAWN:{
                    cb.board[locRank][destFile] = Constants.EMPTY_SQUARE;
                    cb.pieceLocations.remove((Object)(destFile+locRank*8));
                }
            }
            cb.board[destRank][destFile] = cb.board[locRank][locFile];
            cb.board[locRank][locFile] = Constants.EMPTY_SQUARE;
            cb.pieceLocations.add(destFile + destRank * 8);
            cb.pieceLocations.remove((Object)(locFile+locRank*8));
            cb.fenParts[locRank] = FenUtils.getRank(cb.board[locRank]);
            cb.fenParts[destRank] = FenUtils.getRank(cb.board[destRank]);
            cb.turn  = cb.turn  == Constants.WHITE?Constants.BLACK:Constants.WHITE;
            cb.fenParts[10] = "-";
            cb.fenParts[8] = Character.toString(cb.turn);
        }

        else{
            int locFile = Integer.parseInt(String.valueOf(move.charAt(0)));
            int destFile = Integer.parseInt(String.valueOf(move.charAt(2)));
            int locRank = Integer.parseInt(String.valueOf(move.charAt(1)));
            int destRank = Integer.parseInt(String.valueOf(move.charAt(3)));
            if(cb.board[locRank][locFile] == Constants.WHITE_KING){
                cb.whiteKingPosition[0] = destFile;
                cb.whiteKingPosition[1] = destRank;
            }else if(cb.board[locRank][locFile] == Constants.BLACK_KING){
                cb.blackKingPosition[0] = destFile;
                cb.blackKingPosition[1] = destRank;
            }

            if(locRank == 0 || locRank == 7){
                switch(cb.board[locRank][locFile]){
                    case Constants.BLACK_KING:{
                        if(cb.fenParts[9].contains(String.valueOf(Constants.BLACK_QUEEN)) || cb.fenParts[9].contains(String.valueOf(Constants.BLACK_KING))) {
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.BLACK_KING), "").replace(Character.toString(Constants.BLACK_QUEEN), "");
                        }
                        break;
                    }
                    case Constants.WHITE_KING:{
                        if(cb.fenParts[9].contains(String.valueOf(Constants.WHITE_QUEEN)) || cb.fenParts[9].contains(String.valueOf(Constants.WHITE_KING))) {
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.WHITE_KING), "").replace(Character.toString(Constants.WHITE_QUEEN), "");
                        }
                        break;
                    }
                }
            }
            if(destRank == 0 || destRank == 7){
                switch(cb.board[destRank][destFile]){
                    case Constants.WHITE_ROOK:
                        if(destFile==0 && cb.fenParts[9].contains(String.valueOf(Constants.WHITE_QUEEN))){
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.WHITE_QUEEN),"");
                        }else if(destFile==7 && cb.fenParts[9].contains(String.valueOf(Constants.WHITE_KING))){
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.WHITE_KING), "");
                        }
                        break;
                    case Constants.BLACK_ROOK:
                        if(destFile==0 && cb.fenParts[9].contains(String.valueOf(Constants.BLACK_QUEEN))){
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.BLACK_QUEEN),"");
                        }else if(destFile==7 && cb.fenParts[9].contains(String.valueOf(Constants.BLACK_KING))) {
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.BLACK_KING), "");
                        }
                        break;
                }
                if(Character.toUpperCase(cb.board[locRank][locFile]) == Constants.WHITE_PAWN){
                    cb.board[locRank][locFile] = move.split(Constants.MOVE_SEPARATOR)[4].charAt(0);
                }
            }


            cb.board[destRank][destFile] = cb.board[locRank][locFile];
            cb.board[locRank][locFile] = Constants.EMPTY_SQUARE;
            cb.pieceLocations.add(destFile + destRank * 8);
            cb.pieceLocations.remove((Object)(locFile+locRank*8));
            cb.fenParts[locRank] = FenUtils.getRank(cb.board[locRank]);
            cb.fenParts[destRank] = FenUtils.getRank(cb.board[destRank]);
            cb.turn  = cb.turn  == Constants.WHITE?Constants.BLACK:Constants.WHITE;
            cb.fenParts[10] = "-";
            cb.fenParts[8] = Character.toString(cb.turn);
        }
        cb.checkBoard();
        if(cb.fenParts[9].equals(" ")){
            cb.fenParts[9] = "-";
        }
    }
    public void undoMove(String move){
        if(move.contains(Constants.KING_SIDE_CASTLING)){
            int rank = cb.turn == Constants.WHITE?7:0;
            cb.board[rank][4] = cb.board[rank][6];
            cb.board[rank][6] = Constants.EMPTY_SQUARE;
            cb.board[rank][7] = cb.board[rank][5];
            cb.board[rank][5] = Constants.EMPTY_SQUARE;
            cb.pieceLocations.add(4 + rank * 8);
            cb.pieceLocations.remove((Object)(6 + rank * 8));
            cb.pieceLocations.add(7 + rank * 8);
            cb.pieceLocations.remove((Object)(5 + rank * 8));
            if(cb.turn == Constants.WHITE){
                cb.whiteKingPosition[0] = 4;
            }else{
                cb.blackKingPosition[0] = 4;
            }
            cb.fenParts[rank] = FenUtils.getRank(cb.board[rank]);

            String[] moveParts = move.split(Constants.MOVE_SEPARATOR);

            cb.fenParts[9] = moveParts[1];
            cb.fenParts[10] = moveParts[2];
            cb.turn  = cb.turn  == Constants.WHITE?Constants.BLACK:Constants.WHITE;
            cb.fenParts[8] = Character.toString(cb.turn);
        }else if(move.contains(Constants.QUEEN_SIDE_CASTLING)){
            int rank = cb.turn == Constants.WHITE?7:0;
            cb.board[rank][4] = cb.board[rank][2];
            cb.board[rank][2] = Constants.EMPTY_SQUARE;
            cb.board[rank][0] = cb.board[rank][3];
            cb.board[rank][3] = Constants.EMPTY_SQUARE;
            cb.pieceLocations.add(4 + rank * 8);
            cb.pieceLocations.remove((Object)(2 + rank * 8));
            cb.pieceLocations.add(0 + rank * 8);
            cb.pieceLocations.remove((Object)(3 + rank * 8));
            if(cb.turn == Constants.WHITE){
                cb.whiteKingPosition[0] = 4;
            }else{
                cb.blackKingPosition[0] = 4;
            }
            cb.fenParts[rank] = FenUtils.getRank(cb.board[rank]);
            String[] moveParts = move.split(Constants.MOVE_SEPARATOR);
            cb.fenParts[9] = moveParts[1];
            cb.fenParts[10] = moveParts[2];
            cb.turn  = cb.turn  == Constants.WHITE?Constants.BLACK:Constants.WHITE;
            cb.fenParts[8] = Character.toString(cb.turn);
        }else if(move.charAt(1) == move.charAt(3)){
            int rank = Integer.parseInt(String.valueOf(move.charAt(1)));
            int locFile = Integer.parseInt(String.valueOf(move.charAt(2)));
            int destFile = Integer.parseInt(String.valueOf(move.charAt(0)));
            if(cb.board[rank][locFile] == Constants.WHITE_KING){
                cb.whiteKingPosition[0] = destFile;
                cb.whiteKingPosition[1] = rank;
            }else if(cb.board[rank][locFile] == Constants.BLACK_KING){
                cb.blackKingPosition[0] = destFile;
                cb.blackKingPosition[1] = rank;
            }

            String[] moveParts = move.split(Constants.MOVE_SEPARATOR);

            cb.fenParts[10] = moveParts[3];

            if(rank == 0 || rank == 7){
                switch(cb.board[rank][locFile]){
                    case Constants.WHITE_ROOK:

                    case Constants.WHITE_KING:

                    case Constants.BLACK_ROOK:

                    case Constants.BLACK_KING:
                        cb.fenParts[9] = moveParts[2];
                }

            }

            cb.board[rank][destFile] = cb.board[rank][locFile];
            cb.board[rank][locFile] = moveParts[1].charAt(0);
            cb.pieceLocations.add(destFile + rank * 8);
            if(moveParts[1].charAt(0) == Constants.EMPTY_SQUARE) {
                cb.pieceLocations.remove((Object)(locFile + rank * 8));
            }
            cb.fenParts[rank] = FenUtils.getRank(cb.board[rank]);
            cb.turn  = cb.turn  == Constants.WHITE?Constants.BLACK:Constants.WHITE;
            cb.fenParts[8] = Character.toString(cb.turn);

        }else if(move.charAt(0) == move.charAt(2)){
            int file = Integer.parseInt(String.valueOf(move.charAt(0)));
            int locRank = Integer.parseInt(String.valueOf(move.charAt(3)));
            int destRank = Integer.parseInt(String.valueOf(move.charAt(1)));
            if(cb.board[locRank][file] == Constants.WHITE_KING){
                cb.whiteKingPosition[0] = file;
                cb.whiteKingPosition[1] = destRank;
            }else if(cb.board[locRank][file] == Constants.BLACK_KING){
                cb.blackKingPosition[0] = file;
                cb.blackKingPosition[1] = destRank;
            }

            String[] moveParts = move.split(Constants.MOVE_SEPARATOR);

            cb.fenParts[10] = moveParts[3];

            if(locRank == 0 || locRank == 7){
                if(moveParts.length==5){
                    switch(cb.turn){
                        case Constants.WHITE:
                            cb.board[locRank][file] = Constants.WHITE_PAWN;
                            break;
                        case Constants.BLACK:
                            cb.board[locRank][file] = Constants.BLACK_PAWN;
                            break;
                    }
                }

            }

            cb.board[destRank][file] = cb.board[locRank][file];
            cb.board[locRank][file] = moveParts[1].charAt(0);
            cb.pieceLocations.add(file + destRank * 8);
            if(moveParts[1].charAt(0) == Constants.EMPTY_SQUARE) {
                cb.pieceLocations.remove((Object)(file+locRank*8));
            }
            cb.fenParts[locRank] = FenUtils.getRank(cb.board[locRank]);
            cb.fenParts[destRank] = FenUtils.getRank(cb.board[destRank]);
            cb.turn  = cb.turn  == Constants.WHITE?Constants.BLACK:Constants.WHITE;
            cb.fenParts[8] = Character.toString(cb.turn);
            cb.fenParts[9] = moveParts[2];
        }
        else if(move.contains(Constants.EN_PASSANT_NOTATION)){
            int locFile = Integer.parseInt(String.valueOf(move.charAt(2)));
            int destFile = Integer.parseInt(String.valueOf(move.charAt(0)));
            int locRank = Integer.parseInt(String.valueOf(move.charAt(3)));
            int destRank = Integer.parseInt(String.valueOf(move.charAt(1)));
            switch(cb.board[locRank][locFile]){
                case Constants.WHITE_PAWN:{
                    cb.board[locRank][destFile] = Constants.BLACK_PAWN;
                    cb.pieceLocations.add((destFile+locRank*8));
                    break;
                }
                case Constants.BLACK_PAWN:{
                    cb.board[locRank][destFile] = Constants.WHITE_PAWN;
                    cb.pieceLocations.add((destFile+locRank*8));
                }
            }
            cb.board[destRank][destFile] = cb.board[locRank][locFile];
            cb.board[locRank][locFile] = Constants.EMPTY_SQUARE;
            cb.pieceLocations.add(destFile + destRank * 8);
            cb.pieceLocations.remove((Object)(locFile+locRank*8));
            cb.fenParts[locRank] = FenUtils.getRank(cb.board[locRank]);
            cb.fenParts[destRank] = FenUtils.getRank(cb.board[destRank]);
            cb.turn  = cb.turn  == Constants.WHITE?Constants.BLACK:Constants.WHITE;
            cb.fenParts[10] = move.split(Constants.MOVE_SEPARATOR)[3];
            cb.fenParts[8] = Character.toString(cb.turn);
        }

        else{
            int locFile = Integer.parseInt(String.valueOf(move.charAt(2)));
            int destFile = Integer.parseInt(String.valueOf(move.charAt(0)));
            int locRank = Integer.parseInt(String.valueOf(move.charAt(3)));
            int destRank = Integer.parseInt(String.valueOf(move.charAt(1)));
            if(cb.board[locRank][locFile] == Constants.WHITE_KING){
                cb.whiteKingPosition[0] = destFile;
                cb.whiteKingPosition[1] = destRank;
            }else if(cb.board[locRank][locFile] == Constants.BLACK_KING){
                cb.blackKingPosition[0] = destFile;
                cb.blackKingPosition[1] = destRank;
            }

            String[] moveParts = move.split(Constants.MOVE_SEPARATOR);
            cb.fenParts[10] = moveParts[3];

            if(locRank == 0 || locRank == 7){
                if(moveParts.length==5){
                    switch(cb.turn){
                        case Constants.WHITE:
                            cb.board[locRank][locFile] = Constants.WHITE_PAWN;
                            break;
                        case Constants.BLACK:
                            cb.board[locRank][locFile] = Constants.BLACK_PAWN;
                            break;
                    }
                }
            }

            cb.board[destRank][destFile] = cb.board[locRank][locFile];
            cb.board[locRank][locFile] = moveParts[1].charAt(0);
            cb.pieceLocations.add(destFile + destRank * 8);
            if(moveParts[1].charAt(0) == Constants.EMPTY_SQUARE) {
                cb.pieceLocations.remove((Object) (locFile + locRank * 8));
            }
            cb.fenParts[locRank] = FenUtils.getRank(cb.board[locRank]);
            cb.fenParts[destRank] = FenUtils.getRank(cb.board[destRank]);
            cb.turn  = cb.turn  == Constants.WHITE?Constants.BLACK:Constants.WHITE;
            cb.fenParts[8] = Character.toString(cb.turn);
            cb.fenParts[9] = moveParts[2];

        }
        cb.checkBoard();
    }

    //ends here





    public ArrayList<String> generateMove(int file, int rank){
        moves.clear();
        switch(Character.toUpperCase(cb.board[rank][file])){
            case Constants.WHITE_KING:
                return king(file,rank);
            case Constants.WHITE_PAWN:
                return pawn(file,rank);
            case Constants.WHITE_ROOK:
                return rook(file,rank);
            case Constants.WHITE_BISHOP:
                return bishop(file,rank);
            case Constants.WHITE_KNIGHT:
                return knight(file,rank);
            case Constants.WHITE_QUEEN:
                return queen(file,rank);
            default:
                return null;
        }
    }

    public ArrayList<String> king(final int file,final int rank){
        if(cb.gs == GameState.CHECK){
            ArrayList<int[]> checkDirections = new ArrayList<>();
            for(Integer checkerIndex:cb.checkers.keySet()){
                int checkerFile = checkerIndex%8;
                int checkerRank = checkerIndex/8;
                switch(Util.toUpper(cb.board[checkerRank][checkerFile])){
                    case Constants.WHITE_KNIGHT:
                        checkDirections.add(Constants.KNIGHT_DIRECTION[cb.checkers.get(checkerIndex)]);
                        break;
                    default:
                        checkDirections.add(Constants.ALL_DIRECTIONS[cb.checkers.get(checkerIndex)]);
                }
            }
            for(int[] direction:Constants.ALL_DIRECTIONS){
                int newFile = file+direction[0];
                int newRank = rank+direction[1];
                if(!Util.isValid(newFile,newRank)){
                    continue;
                }
                if(!cb.checkers.containsKey(file+direction[0]+(rank+direction[1])*8)&&checkDirections.contains(direction)){
                    continue;
                }
                boolean pruneDirection = false;
                for(int[] checkDir:checkDirections){
                    if(direction[0] == -checkDir[0] && direction[1] == -checkDir[1]){
                        pruneDirection = true;
                    }
                }

                if(pruneDirection){
                    continue;
                }

                if(cb.board[newRank][newFile] != Constants.EMPTY_SQUARE&&Util.isAlly(cb.board[rank][file],cb.board[newRank][newFile])){
                    continue;
                }

                if(!cb.squareUnderAttack(newFile,newRank)){
                    moves.add(Util.cvtMove(file,rank,newFile,newRank,cb.board,cb.fenParts));
                }
            }
            return moves;
        }
        for(int[] direction:Constants.ALL_DIRECTIONS){
            int newFile = file+direction[0];
            int newRank = rank+direction[1];
            if(!Util.isValid(newFile,newRank)){
                continue;
            }
            if(cb.board[newRank][newFile] != Constants.EMPTY_SQUARE&&Util.isAlly(cb.board[rank][file],cb.board[newRank][newFile])){
                continue;
            }
            if(!cb.squareUnderAttack(newFile,newRank)){
                moves.add(Util.cvtMove(file,rank,newFile,newRank,cb.board,cb.fenParts));
            }
        }
        // castling
        boolean kingSide=cb.gs!=GameState.CHECK,queenSide = cb.gs!=GameState.CHECK;
        if(cb.turn == Constants.WHITE){
            kingSide = kingSide && cb.fenParts[9].contains(Character.toString(Constants.WHITE_KING));
            queenSide = queenSide && cb.fenParts[9].contains(Character.toString(Constants.WHITE_QUEEN));
        }else{
            kingSide = kingSide && cb.fenParts[9].contains(Character.toString(Constants.BLACK_KING));
            queenSide = queenSide && cb.fenParts[9].contains(Character.toString(Constants.BLACK_QUEEN));
        }
        if(kingSide){
            for(int i=file+1;kingSide&&i<file+3;i++){
                kingSide = cb.board[rank][i] == Constants.EMPTY_SQUARE && !cb.squareUnderAttack(i, rank);
            }
            if(kingSide){
                moves.add(Constants.KING_SIDE_CASTLING+Constants.MOVE_SEPARATOR+cb.fenParts[9]+Constants.MOVE_SEPARATOR+cb.fenParts[10]);
            }
        }
        if(queenSide){
            for(int i=file-1;queenSide&&i>file-3;i--){
                queenSide = cb.board[rank][i] == Constants.EMPTY_SQUARE&&!cb.squareUnderAttack(i,rank);
            }
            if(queenSide){
                moves.add(Constants.QUEEN_SIDE_CASTLING+Constants.MOVE_SEPARATOR+cb.fenParts[9]+Constants.MOVE_SEPARATOR+cb.fenParts[10]);
            }
        }
        return moves;
    }

    public ArrayList<String> pawn(int file,int rank){
        int pinnedIndex = file + rank * 8;
        boolean pinned = cb.pinnedPieces.containsKey(pinnedIndex);
        if(pinned){
            if(cb.gs == GameState.CHECK){
                return moves; // pinned pieces cannot resolve check
            }
            int[] pinDirection = Constants.ALL_DIRECTIONS[cb.pinnedPieces.get(pinnedIndex)];
            if(pinDirection[0] != 0 ){
                if(pinDirection[1] != 0){
                    // only possible move is to capture the pinner
                    if((Util.isUpperCase(cb.board[rank][file]) && pinDirection[1] == -1) || (!Util.isUpperCase(cb.board[rank][file]) && pinDirection[1] == 1)){
                        int df = file + pinDirection[0],dr = rank + pinDirection[1];
                        if(cb.board[dr][df] != Constants.EMPTY_SQUARE){
                            moves.add(Util.cvtMove(file,rank,df,dr,cb.board,cb.fenParts));
                        }
                    }
                }
                return moves;
            }else{
                // generate pushes, that is done below
            }
        }else if(cb.gs == GameState.CHECK){
            if(cb.checkers.size()>1){
                return moves; // a two-way check cannot be resolved without the king moving to a safe square
            }
            int checkerIndex=0;
            for(Integer i:cb.checkers.keySet()){
                checkerIndex = i;
            }
            int checkerFile = checkerIndex % 8;
            int checkerRank = checkerIndex / 8;



            switch(Character.toUpperCase(cb.board[checkerRank][checkerFile])){
                case Constants.WHITE_KNIGHT:
                case Constants.WHITE_PAWN:
                    //no way to block the check, capture move generation is done below
                    break;
                default:
                    int[] kingPosition = cb.kingPosition();
                    int[] checkDirection = Constants.ALL_DIRECTIONS[cb.checkers.get(checkerIndex)];
                    if (checkDirection[0] == 0) {
                        if (Math.abs(file - checkerFile) == 1) {
                            // only possible move is to capture the checker why? https://lichess.org/editor/4k3/8/3q4/4P3/8/3K4/8/8_w_-_-_0_1?color=white
                            //capture move generation is done below
                        }
                    }else{
                        ylglyijg
                    }
                    break;
            }


            if(Math.abs(file-checkerFile) == 1) {
                if ((cb.turn == Constants.WHITE &&rank-checkerRank==1) || (cb.turn == Constants.BLACK &&rank-checkerRank==-1)){
                    moves.add(Util.cvtMove(file,rank,checkerFile,checkerRank,cb.board,cb.fenParts));
                }
            }


//            if(Character.toUpperCase(cb.board[checkerRank][checkerFile]) != Constants.WHITE_KNIGHT && Character.toUpperCase(cb.board[checkerRank][checkerFile]) != Constants.WHITE_PAWN) {
//                int[] checkDirection = Constants.ALL_DIRECTIONS[cb.checkers.get(checkerIndex)];
//                if (checkDirection[0] == 0) {
//                    if (Math.abs(file - checkerFile) == 1) {
//                        // only possible move is to capture the checker why? https://lichess.org/editor/4k3/8/3q4/4P3/8/3K4/8/8_w_-_-_0_1?color=white
//                        if (Math.abs(rank - checkerRank) == 1 && ((Util.isUpperCase(cb.board[rank][file]) && checkDirection[1] == -1) || (!Util.isUpperCase(cb.board[rank][file]) && checkDirection[1] == 1))) {
//                            moves.add(Util.cvtMove(file, rank, file + checkDirection[0], rank + checkDirection[1],cb.board,cb.fenParts));
//                        }
//                    }
//                } else if (checkDirection[1] == 0) {
//                    boolean hasPotential = (Util.isUpperCase(cb.board[rank][file]) && kingPosition[1] - rank == -1) || (!Util.isUpperCase(cb.board[rank][file]) && kingPosition[1] - rank == 1);
//                    if (file == checkerFile) {
//                        // cannot resolve the check, why? https://lichess.org/editor/7k/8/2K3q1/6P1/8/8/8/8_w_-_-_0_1?color=white
//                    }else if(hasPotential){
//                        // possible move is to block the check how? https://lichess.org/editor/7k/8/1K2q3/3P4/8/8/8/8_w_-_-_0_1?color=white
//                        int destRank = cb.turn == Constants.WHITE?rank-1:rank+1;
//                        if(file>kingPosition[0] && file<checkerFile && destRank<8 && destRank>=0 && cb.board[destRank][file] == Constants.EMPTY_SQUARE){
//                            if(destRank == 0 || destRank == 7){
//                                switch(cb.turn){
//                                    case Constants.WHITE:
//                                        moves.add(Util.cvtMove(file, rank, file, destRank, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.WHITE_QUEEN);
//                                        moves.add(Util.cvtMove(file, rank, file, destRank, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.WHITE_KNIGHT);
//                                        moves.add(Util.cvtMove(file, rank, file, destRank, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.WHITE_ROOK);
//                                        moves.add(Util.cvtMove(file, rank, file, destRank, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.WHITE_BISHOP);
//                                        break;
//                                    case Constants.BLACK:
//                                        moves.add(Util.cvtMove(file, rank, file, destRank, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.BLACK_QUEEN);
//                                        moves.add(Util.cvtMove(file, rank, file, destRank, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.BLACK_KNIGHT);
//                                        moves.add(Util.cvtMove(file, rank, file, destRank, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.BLACK_ROOK);
//                                        moves.add(Util.cvtMove(file, rank, file, destRank, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.BLACK_BISHOP);
//                                        break;
//                                }
//
//                            }else {
//                                moves.add(Util.cvtMove(file, rank, file, destRank, cb.board, cb.fenParts));
//                            }
//
//                        }
//                        if(Math.abs(file-checkerFile) == 1) {
//                            // possible move is to capture the checker how? https://lichess.org/editor/7k/8/1K2q3/3P4/8/8/8/8_w_-_-_0_1?color=white
//                            moves.add(Util.cvtMove(file, rank, checkerFile, checkerRank,cb.board,cb.fenParts));
//                        }
//                    }
//                }
//            }else{
//                // no way to block a check from knight or pawn, only possible move is to capture
//                if(Math.abs(checkerFile-file) == 1 && Math.abs(checkerRank-rank) == 1){
//                    if((cb.turn == Constants.WHITE && checkerRank<rank) || (cb.turn == Constants.BLACK && checkerRank>rank)){
//                        if(checkerRank == 0 || checkerRank == 7){
//                            switch(cb.turn){
//                                case Constants.WHITE:
//                                    moves.add(Util.cvtMove(file, rank, checkerFile, checkerRank, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.WHITE_QUEEN);
//                                    moves.add(Util.cvtMove(file, rank, checkerFile, checkerRank, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.WHITE_KNIGHT);
//                                    moves.add(Util.cvtMove(file, rank, checkerFile, checkerRank, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.WHITE_ROOK);
//                                    moves.add(Util.cvtMove(file, rank, checkerFile, checkerRank, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.WHITE_BISHOP);
//                                    break;
//                                case Constants.BLACK:
//                                    moves.add(Util.cvtMove(file, rank, checkerFile, checkerRank, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.BLACK_QUEEN);
//                                    moves.add(Util.cvtMove(file, rank, checkerFile, checkerRank, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.BLACK_KNIGHT);
//                                    moves.add(Util.cvtMove(file, rank, checkerFile, checkerRank, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.BLACK_ROOK);
//                                    moves.add(Util.cvtMove(file, rank, checkerFile, checkerRank, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.BLACK_BISHOP);
//                                    break;
//                            }
//
//                        }else {
//                            moves.add(Util.cvtMove(file,rank,checkerFile,checkerRank,cb.board,cb.fenParts));
//                        }
//                    }
//                }else if(!cb.fenParts[10].equals("-")){
//                    // en passant move to resolve the check!
//                    if(Math.abs(Constants.FILES.indexOf(cb.fenParts[10].charAt(0)) - file) == 1){
//                        if(Util.isUpperCase(cb.board[rank][file])){
//                            if(rank-1 == 8-Integer.parseInt(Character.toString(cb.fenParts[10].charAt(10)))){
//                                moves.add(Util.cvtMove(file, rank, Constants.FILES.indexOf(cb.fenParts[10].charAt(0)), 2,cb.board,cb.fenParts)+Constants.MOVE_SEPARATOR+"en");
//                            }
//                        }else{
//                            if(rank+1 == 8-Integer.parseInt(Character.toString(cb.fenParts[10].charAt(10)))){
//                                moves.add(Util.cvtMove(file, rank, Constants.FILES.indexOf(cb.fenParts[10].charAt(0)), 5,cb.board,cb.fenParts)+Constants.MOVE_SEPARATOR+"en");
//                            }
//                        }
//                    }
//
//                }
//            }
            return moves;
        }


        // generating pushes
        int f=file,r=rank;
        int startIndex = Util.isUpperCase(cb.board[rank][file])?3:2;
        int endIndex = (Util.isUpperCase(cb.board[rank][file]) && rank == 6) || (!Util.isUpperCase(cb.board[rank][file]) && rank == 1)?2:1;


        for(int i=0;i<endIndex;i++){// needs to be fixed
            //System.out.println(i);
            f += Constants.ALL_DIRECTIONS[startIndex][0];
            r += Constants.ALL_DIRECTIONS[startIndex][1];
            if(Util.isValid(f,r)){

                if(cb.board[r][f] == Constants.EMPTY_SQUARE){
                    if(r == 0 || r == 7){
                        switch(cb.turn){
                            case Constants.WHITE:
                                moves.add(Util.cvtMove(file, rank, f, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.WHITE_QUEEN);
                                moves.add(Util.cvtMove(file, rank, f, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.WHITE_KNIGHT);
                                moves.add(Util.cvtMove(file, rank, f, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.WHITE_ROOK);
                                moves.add(Util.cvtMove(file, rank, f, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.WHITE_BISHOP);
                                break;
                            case Constants.BLACK:
                                moves.add(Util.cvtMove(file, rank, f, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.BLACK_QUEEN);
                                moves.add(Util.cvtMove(file, rank, f, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.BLACK_KNIGHT);
                                moves.add(Util.cvtMove(file, rank, f, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.BLACK_ROOK);
                                moves.add(Util.cvtMove(file, rank, f, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.BLACK_BISHOP);
                                break;
                        }

                    }else {
                        moves.add(Util.cvtMove(file,rank,f,r,cb.board,cb.fenParts));
                    }
                }else {
                    break;
                }

            }else{
                break;
            }


        }


        if(pinned){
            return moves; // a pawn pinned vertically cannot move diagonally
        }

        // generating diagonal moves
        startIndex = Util.isUpperCase(cb.board[rank][file])?6:4;
        endIndex = Util.isUpperCase(cb.board[rank][file])?Constants.ALL_DIRECTIONS.length-1:5;

        for(int i=startIndex;i<=endIndex;i++){
            f = file + Constants.ALL_DIRECTIONS[i][0];
            r = rank + Constants.ALL_DIRECTIONS[i][1];
            if(Util.isValid(f,r)){
                if(cb.board[r][f] != Constants.EMPTY_SQUARE && Util.isEnemyPiece(cb.turn,cb.board[r][f])){

                    if(r == 0 || r == 7){
                        switch(cb.turn){
                            case Constants.WHITE:
                                moves.add(Util.cvtMove(file, rank, f, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.WHITE_QUEEN);
                                moves.add(Util.cvtMove(file, rank, f, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.WHITE_KNIGHT);
                                moves.add(Util.cvtMove(file, rank, f, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.WHITE_ROOK);
                                moves.add(Util.cvtMove(file, rank, f, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.WHITE_BISHOP);
                                break;
                            case Constants.BLACK:
                                moves.add(Util.cvtMove(file, rank, f, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.BLACK_QUEEN);
                                moves.add(Util.cvtMove(file, rank, f, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.BLACK_KNIGHT);
                                moves.add(Util.cvtMove(file, rank, f, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.BLACK_ROOK);
                                moves.add(Util.cvtMove(file, rank, f, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.BLACK_BISHOP);
                                break;
                        }

                    }else {
                        moves.add(Util.cvtMove(file,rank,f,r,cb.board,cb.fenParts));
                    }
                }
            }
        }

        if(cb.fenParts[10].equals("-")){
            // do nothing
        }else{
            int dr = cb.turn == Constants.WHITE?2:5;
            if(Math.abs(file-Constants.FILES.indexOf(cb.fenParts[10].charAt(0))) == 1){
                int[] kingPosition = cb.kingPosition();
                int enPassantPawn = Constants.FILES.indexOf(cb.fenParts[10].charAt(0)),direction = Util.getSign(file,kingPosition[0]);
                boolean enPassant = true;
                if((!Util.isUpperCase(cb.board[rank][file]) && rank == 4)||(Util.isUpperCase(cb.board[rank][file]) && rank == 3)){
                    if(kingPosition[1] == rank){
                        // possible occurrence https://lichess.org/editor/r2k3r/4p1pp/8/2K1Pp1q/8/8/PP1P1PP1/R7_w_k_-_0_1?color=white
                        // en passant reveals a check
                        // make en-passant move
                        boolean foundAnotherPiece = true;
                        for(int i=file+direction;i!=kingPosition[0];i+=direction){
                            foundAnotherPiece = cb.board[rank][i]!=Constants.EMPTY_SQUARE&&Character.toUpperCase(cb.board[rank][i]) != Constants.WHITE_KING;
                            if(foundAnotherPiece){
                                break;
                            }
                        }
                        if(!foundAnotherPiece){
                            //loop to the other side and see if there is an opponent rook or queen
                            for(int i=enPassantPawn-direction;i>0&&i<8;i-=direction){
                                if(cb.board[rank][i] != Constants.EMPTY_SQUARE){
                                    if(!Util.isAlly(cb.board[rank][file],cb.board[rank][i]) && (Character.toUpperCase(cb.board[rank][i]) == Constants.WHITE_ROOK || Character.toUpperCase(cb.board[rank][i]) == Constants.WHITE_QUEEN)){
                                        //en-passant reveals check
                                        enPassant = false;
                                    }
                                    break;
                                }
                            }
                        }
                        if(enPassant){
                            moves.add(Util.cvtMove(file, rank, Constants.FILES.indexOf(cb.fenParts[10].charAt(0)), dr,cb.board,cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.EN_PASSANT_NOTATION);
                        }
                    }else {
                        moves.add(Util.cvtMove(file, rank, Constants.FILES.indexOf(cb.fenParts[10].charAt(0)), dr,cb.board,cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.EN_PASSANT_NOTATION);
                    }
                }
            }
        }

        return moves;
    }

    public ArrayList<String> queen(final int file,final int rank){//complete
        int pinnedIndex = file + rank * 8;
        if(cb.pinnedPieces.containsKey(pinnedIndex)){
            if(cb.gs == GameState.CHECK){
                return moves; // a pinned piece cannot resolve check
            }
            int[] pinDirection = Constants.ALL_DIRECTIONS[cb.pinnedPieces.get(pinnedIndex)];
            // can move along pinned squares
            boolean foundKing=false,foundEnemyPiece=false;
            int df=file,dr=rank;
            while(!foundEnemyPiece || !foundKing){
                if(!foundEnemyPiece){
                    df += pinDirection[0];
                    dr += pinDirection[1];
                    if(cb.board[dr][df] == Constants.EMPTY_SQUARE) {
                        moves.add(Util.cvtMove(file, rank, df, dr,cb.board,cb.fenParts));
                    }else{
                        foundEnemyPiece = true;
                        moves.add(Util.cvtMove(file, rank, df, dr,cb.board,cb.fenParts));
                        df = file;
                        dr = rank;
                    }
                }else {
                    df -= pinDirection[0];
                    dr -= pinDirection[1];
                    if(cb.board[dr][df] == Constants.EMPTY_SQUARE) {
                        moves.add(Util.cvtMove(file, rank, df, dr,cb.board,cb.fenParts));
                    }else{
                        foundKing = true;
                    }
                }
            }
            return moves;
        }else if(cb.gs == GameState.CHECK){
            if(cb.checkers.size()>1){
                return moves;// a two-way check cannot be resolved without the king moving to a safe square
            }
            int checkerIndex=0;
            for(Integer i:cb.checkers.keySet()){
                checkerIndex = i;
            }
            int checkerFile = checkerIndex % 8;
            int checkerRank = checkerIndex / 8;
            if(Character.toUpperCase(cb.board[checkerRank][checkerFile]) != Constants.WHITE_KNIGHT || Character.toUpperCase(cb.board[checkerRank][checkerFile]) != Constants.WHITE_PAWN){
                int[] kingPosition = cb.kingPosition();
                int[] checkDirection = Constants.ALL_DIRECTIONS[cb.checkers.get(checkerIndex)];

                if(checkDirection[1] == 0){
                    //bishop part
                    for(int i=checkerFile;Util.inBetween(checkerFile,kingPosition[0],i);i-=checkDirection[0]){
                        if((i+checkerRank)%2 == (file+rank)%2 && cb.canSlide(file,rank,i,checkerRank)){
                            moves.add(Util.cvtMove(file,rank,i,checkerRank,cb.board,cb.fenParts));
                        }
                    }

                    //rook
                    if(Util.inBetween(kingPosition[0],checkerFile,file)){
                        // means the rook is in between the checker and the king, therefore it might be able to block the check
                        if(cb.canSlide(file,rank,file,kingPosition[1])){
                            moves.add(Util.cvtMove(file,rank,file,kingPosition[1],cb.board,cb.fenParts));
                        }
                    }
                }else if(checkDirection[0] == 0){
                    //bishop
                    for(int i=checkerRank;Util.inBetween(checkerRank,kingPosition[1],i);i-=checkDirection[1]){
                        if((i+checkerRank)%2 == (file+rank)%2 && cb.canSlide(file,rank,checkerFile,i)){
                            moves.add(Util.cvtMove(file,rank,checkerFile,i,cb.board,cb.fenParts));
                        }
                    }

                    //rook
                    if(Util.inBetween(kingPosition[1],checkerRank,rank)){
                        // means the rook is in between the checker and the king, therefore it might be able to block the check
                        if(cb.canSlide(file,rank,kingPosition[0],rank)){
                            moves.add(Util.cvtMove(file,rank,kingPosition[0],rank,cb.board,cb.fenParts));
                        }
                    }
                }else{

                    //rook
                    if(Util.inBetween(kingPosition[0],checkerFile,file)){
                        int f = checkerFile-checkDirection[0],r = checkerRank-checkDirection[1];
                        while(Util.inBetween(f+checkDirection[0],kingPosition[0],f)){
                            if(f == file){
                                if(cb.canSlide(file,rank,f,r)) {
                                    moves.add(Util.cvtMove(file, rank, f, r,cb.board,cb.fenParts));
                                }
                                break;
                            }
                            f -= checkDirection[0];
                            r -= checkDirection[1];
                        }
                    }
                    if(Util.inBetween(kingPosition[1],checkerRank,rank)){
                        int f = checkerFile-checkDirection[0],r = checkerRank-checkDirection[1];
                        while(Util.inBetween(r+checkDirection[1],kingPosition[1],r)){
                            if(r == rank){
                                if(cb.canSlide(file,rank,f,r)) {
                                    moves.add(Util.cvtMove(file, rank, f, r,cb.board,cb.fenParts));
                                }
                                break;
                            }
                            f -= checkDirection[0];
                            r -= checkDirection[1];
                        }
                    }

                    //bishop
                    if((file+rank)%2 == (checkerFile+checkerRank)%2){
                        int f = checkerFile,r = checkerRank;
                        while(Util.inBetween(f,kingPosition[0],f) && Util.inBetween(r,kingPosition[1],r)){
                            if(rank-file == r-f){
                                if(cb.canSlide(file,rank,f,r)) {
                                    moves.add(Util.cvtMove(file, rank, f, r,cb.board,cb.fenParts));
                                }
                                break;
                            }
                            f -= checkDirection[0];
                            r -= checkDirection[1];
                        }
                    }
                }
            }else{
                // no way to block the check, only possible move is to capture
            }
            if(cb.canSlide(checkerFile,checkerRank,file,rank)){
                // capture
                moves.add(Util.cvtMove(file,rank,checkerFile,checkerRank,cb.board,cb.fenParts));
            }
            return moves;
        }

        // generate normal moves
        int df,dr;
        for(int[] direction:Constants.ALL_DIRECTIONS){
            df = file + direction[0];
            dr = rank + direction[1];
            while(Util.isValid(df,dr)){
                if(cb.board[dr][df] == Constants.EMPTY_SQUARE){
                    moves.add(Util.cvtMove(file,rank,df,dr,cb.board,cb.fenParts));
                }else{
                    if(Util.isAlly(cb.board[rank][file],cb.board[dr][df])){

                    }else{
                        moves.add(Util.cvtMove(file,rank,df,dr,cb.board,cb.fenParts));
                    }
                    break;
                }
                df += direction[0];
                dr += direction[1];
            }
        }
        return moves;
    }

    public ArrayList<String> rook(final int file,final int rank){//complete
        int pinnedIndex = file + rank * 8;
        if(cb.pinnedPieces.containsKey(pinnedIndex)){
            if(cb.gs == GameState.CHECK){
                return moves;
            }
            int[] pinDirection = Constants.ALL_DIRECTIONS[cb.pinnedPieces.get(pinnedIndex)];
            if(pinDirection[0] != 0 && pinDirection[1] != 0){
                // a rook pinned by bishop or diagonally by queen cannot move
            }else{
                // can move along pinned squares
                boolean foundKing=false,foundEnemyPiece=false;
                int df=file,dr=rank;
                while(!foundEnemyPiece || !foundKing){
                    if(!foundEnemyPiece){
                        df += pinDirection[0];
                        dr += pinDirection[1];
                        if(cb.board[dr][df] == Constants.EMPTY_SQUARE) {
                            moves.add(Util.cvtMove(file, rank, df, dr,cb.board,cb.fenParts));
                        }else{
                            foundEnemyPiece = true;
                            moves.add(Util.cvtMove(file, rank, df, dr,cb.board,cb.fenParts));
                            df = file;
                            dr = rank;
                        }
                    }else {
                        df -= pinDirection[0];
                        dr -= pinDirection[1];
                        if(cb.board[dr][df] == Constants.EMPTY_SQUARE) {
                            moves.add(Util.cvtMove(file, rank, df, dr,cb.board,cb.fenParts));
                        }else{
                            foundKing = true;
                        }
                    }
                }
            }
            return moves;
        }else if(cb.gs==GameState.CHECK){
            if(cb.checkers.size()>1){
                return moves;
            }
            int checkerIndex=0;
            for(Integer i:cb.checkers.keySet()){
                checkerIndex = i;
            }
            int checkerFile = checkerIndex % 8;
            int checkerRank = checkerIndex / 8;
            if(Character.toUpperCase(cb.board[checkerRank][checkerFile]) != Constants.WHITE_KNIGHT && Character.toUpperCase(cb.board[checkerRank][checkerFile]) != Constants.WHITE_PAWN){
                int[] kingPosition = cb.kingPosition();
                int[] checkDirection = Constants.ALL_DIRECTIONS[cb.checkers.get(checkerIndex)];
                if(checkDirection[0] == 0){
                    if(Util.inBetween(kingPosition[1],checkerRank,rank)){
                        // means the rook is in between the checker and the king, therefore it might be able to block the check
                        if(cb.canSlide(file,rank,kingPosition[0],rank)){
                            moves.add(Util.cvtMove(file,rank,kingPosition[0],rank,cb.board,cb.fenParts));
                        }
                    }
                }else if(checkDirection[1] == 0){
                    if(Util.inBetween(kingPosition[0],checkerFile,file)){
                        // means the rook is in between the checker and the king, therefore it might be able to block the check
                        if(cb.canSlide(file,rank,file,kingPosition[1])){
                            moves.add(Util.cvtMove(file,rank,file,kingPosition[1],cb.board,cb.fenParts));
                        }
                    }
                }else{
                    // done

                    if(Util.inBetween(kingPosition[0],checkerFile,file)){
                        int f = checkerFile-checkDirection[0],r = checkerRank-checkDirection[1];
                        while(Util.inBetween(f+checkDirection[0],kingPosition[0],f)){
                            if(f == file){
                                if(cb.canSlide(file,rank,f,r)) {
                                    moves.add(Util.cvtMove(file, rank, f, r,cb.board,cb.fenParts));
                                }
                                break;
                            }
                            f -= checkDirection[0];
                            r -= checkDirection[1];
                        }
                    }
                    if(Util.inBetween(kingPosition[1],checkerRank,rank)){
                        int f = checkerFile-checkDirection[0],r = checkerRank-checkDirection[1];
                        while(Util.inBetween(r+checkDirection[1],kingPosition[1],r)){
                            if(r == rank){
                                if(cb.canSlide(file,rank,f,r)) {
                                    moves.add(Util.cvtMove(file, rank, f, r,cb.board,cb.fenParts));
                                }
                                break;
                            }
                            f -= checkDirection[0];
                            r -= checkDirection[1];
                        }
                    }
                }
            }else{
                // no way to block, only possible move is to capture the checker
            }
            if(file == checkerFile || rank == checkerRank){
                if(cb.canSlide(file,rank,checkerFile,checkerRank)){
                    moves.add(Util.cvtMove(file,rank,checkerFile,checkerRank,cb.board,cb.fenParts));
                }
            }
            return moves;
        }

        // generate normal moves
        int df,dr;
        for(int i=0;i<4;i++){
            df = file + Constants.ALL_DIRECTIONS[i][0];
            dr = rank + Constants.ALL_DIRECTIONS[i][1];
            while(Util.isValid(df,dr)){
                if(cb.board[dr][df] == Constants.EMPTY_SQUARE){
                    moves.add(Util.cvtMove(file,rank,df,dr,cb.board,cb.fenParts));
                }else{
                    if(Util.isAlly(cb.board[rank][file],cb.board[dr][df])){

                    }else{
                        moves.add(Util.cvtMove(file,rank,df,dr,cb.board,cb.fenParts));
                    }
                    break;
                }
                df += Constants.ALL_DIRECTIONS[i][0];
                dr += Constants.ALL_DIRECTIONS[i][1];
            }
        }
        return moves;
    }

    public ArrayList<String> knight(final int file,final int rank){//complete
        if(cb.pinnedPieces.containsKey(file + rank * 8)){
            return moves; // a pinned knight cannot move
        }else if(cb.gs == GameState.CHECK){
            if(cb.checkers.size()>1){
                return moves;
            }
            int checkerIndex=0;
            for(Integer i:cb.checkers.keySet()){
                checkerIndex = i;
            }
            int checkerFile = checkerIndex % 8;
            int checkerRank = checkerIndex / 8;
            if(Character.toUpperCase(cb.board[checkerRank][checkerFile]) != Constants.WHITE_KNIGHT && Character.toUpperCase(cb.board[checkerRank][checkerFile]) != Constants.WHITE_PAWN){
                int[] checkDirection = Constants.ALL_DIRECTIONS[cb.checkers.get(checkerIndex)];
                int[] kingPosition = cb.kingPosition();
                int currentFile=kingPosition[0],currentRank=kingPosition[1];
                while(moves.size()<2 && currentFile!=checkerFile && currentRank!=checkerRank){
                    currentRank+=checkDirection[1];
                    currentFile+=checkDirection[0];
                    if(currentFile == file || currentRank == rank){
                        continue;
                    }
                    int[] direction = Util.getDirection(file,rank,currentFile,currentRank);
                    for(int i=0;i<2;i++){
                        if(file+ Constants.KNIGHT_DIRECTION[i][0]*direction[0] == currentFile && rank+ Constants.KNIGHT_DIRECTION[i][1]*direction[1] == currentRank){
                            moves.add(Util.cvtMove(file,rank,currentFile,currentRank,cb.board,cb.fenParts));
                            break;
                        }
                    }

                }

            }else{
                //only possible move is to capture
                if(checkerFile == file || checkerRank == rank){
                    return moves; // because when the knight moves it changes both, its file and rank, therefore it cannot reach to a tile sharing the same file or rank
                }
                int[] direction = Util.getDirection(file,rank,checkerFile,checkerRank);
                for(int i=0;i<2;i++){
                    if(file+Constants.KNIGHT_DIRECTION[i][0]*direction[0] == checkerFile && rank+Constants.KNIGHT_DIRECTION[i][1]*direction[1] == checkerRank){
                        moves.add(Util.cvtMove(file,rank,checkerFile,checkerRank,cb.board,cb.fenParts));
                        break;
                    }
                }
            }
            return moves;
        }
        int df,dr;
        for(int[] direction:Constants.KNIGHT_DIRECTION){
            df = file + direction[0];
            dr = rank + direction[1];
            if(Util.isValid(df,dr) && !Util.isAlly(cb.board[dr][df],cb.board[rank][file])){
                moves.add(Util.cvtMove(file,rank,df,dr,cb.board,cb.fenParts));
            }
        }

        return moves;
    }

    public ArrayList<String> bishop(final int file,final int rank){//complete!
        int pinnedIndex = file + rank * 8;
        if(cb.pinnedPieces.containsKey(pinnedIndex)){
            if(cb.gs == GameState.CHECK){
                return moves;
            }
            int[] pinDirection = Constants.ALL_DIRECTIONS[cb.pinnedPieces.get(pinnedIndex)];
            if(pinDirection[0] == 0 || pinDirection[1] == 0){
                // a bishop pinned by rook or horizontally by queen cannot move
            }else{
                // can move along pinned squares
                boolean foundKing=false,foundEnemyPiece=false;
                int df=file,dr=rank;
                while(!foundEnemyPiece || !foundKing){
                    if(!foundEnemyPiece){
                        df += pinDirection[0];
                        dr += pinDirection[1];
                        if(cb.board[dr][df] == Constants.EMPTY_SQUARE) {
                            moves.add(Util.cvtMove(file, rank, df, dr,cb.board,cb.fenParts));
                        }else{
                            foundEnemyPiece = true;
                            moves.add(Util.cvtMove(file, rank, df, dr,cb.board,cb.fenParts));
                            df = file;
                            dr = rank;
                        }
                    }else {
                        df -= pinDirection[0];
                        dr -= pinDirection[1];
                        if(cb.board[dr][df] == Constants.EMPTY_SQUARE) {
                            moves.add(Util.cvtMove(file, rank, df, dr,cb.board,cb.fenParts));
                        }else{
                            foundKing = true;
                        }
                    }
                }
            }
            return moves;
        }else if(cb.gs == GameState.CHECK){
            if(cb.checkers.size()>1){
                return moves;
            }
            int checkerIndex=0;
            for(Integer i:cb.checkers.keySet()){
                checkerIndex = i;
            }
            int checkerFile = checkerIndex % 8;
            int checkerRank = checkerIndex / 8;
            if(Character.toUpperCase(cb.board[checkerRank][checkerFile]) != Constants.WHITE_KNIGHT && Character.toUpperCase(cb.board[checkerRank][checkerFile]) != Constants.WHITE_PAWN){
                int[] checkDirection = Constants.ALL_DIRECTIONS[cb.checkers.get(checkerIndex)];
                int[] kingPosition = cb.kingPosition();
                if(checkDirection[1] == 0){
                    for(int i=checkerFile;Util.inBetween(checkerFile+checkDirection[0],kingPosition[0],i);i-=checkDirection[0]){
                        if((i+checkerRank)%2 == (file+rank)%2 && cb.canSlide(file,rank,i,checkerRank)){
                            moves.add(Util.cvtMove(file,rank,i,checkerRank,cb.board,cb.fenParts));
                        }
                    }
                }else if(checkDirection[0] == 0){
                    for(int i=checkerRank;Util.inBetween(checkerRank+checkDirection[1],kingPosition[1],i);i-=checkDirection[1]){
                        if((i+checkerRank)%2 == (file+rank)%2 && cb.canSlide(file,rank,checkerFile,i)){
                            moves.add(Util.cvtMove(file,rank,checkerFile,i,cb.board,cb.fenParts));
                        }
                    }
                }else{
                    if((file+rank)%2 == (checkerFile+checkerRank)%2){
                        int f = checkerFile,r = checkerRank;
                        while(Util.inBetween(f+checkDirection[0],kingPosition[0],f) && Util.inBetween(r+checkDirection[1],kingPosition[1],r)){
                            if(rank-file == r-f){
                                if(cb.canSlide(file,rank,f,r)) {
                                    moves.add(Util.cvtMove(file, rank, f, r,cb.board,cb.fenParts));
                                }
                                break;
                            }
                            f -= checkDirection[0];
                            r -= checkDirection[1];
                        }
                    }
                }
            }else{
                // only possible move is to capture
                if(cb.canSlide(file,rank,checkerFile,checkerRank)){
                    moves.add(Util.cvtMove(file,rank,checkerFile,checkerRank,cb.board,cb.fenParts));
                }
            }


            return moves;
        }

        //normal moves
        int df,dr;
        for(int i = 4; i<Constants.ALL_DIRECTIONS.length; i++){
            df = file + Constants.ALL_DIRECTIONS[i][0];
            dr = rank + Constants.ALL_DIRECTIONS[i][1];
            while(Util.isValid(df,dr)){
                if(cb.board[dr][df] == Constants.EMPTY_SQUARE){
                    moves.add(Util.cvtMove(file,rank,df,dr,cb.board,cb.fenParts));
                }else{
                    if(Util.isAlly(cb.board[rank][file],cb.board[dr][df])){

                    }else{
                        moves.add(Util.cvtMove(file,rank,df,dr,cb.board,cb.fenParts));
                    }
                    break;
                }
                df += Constants.ALL_DIRECTIONS[i][0];
                dr += Constants.ALL_DIRECTIONS[i][1];
            }
        }
        return moves;
    }


}
