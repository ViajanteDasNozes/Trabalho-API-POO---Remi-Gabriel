package br.edu.iftm.testes;

import java.awt.Color;
import java.awt.Image;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.json.JSONArray;
import org.json.JSONObject;

import br.edu.iftm.api.Api;
import br.edu.iftm.api.JsonParser;

public class ApiLolTeste {

	public static void main(String[] args) throws IOException {
		criarJanela();
	}
	
	private static void criarJanela() throws MalformedURLException, IOException {
		final String token = "RGAPI-bdc2e9bb-b5a2-4bb9-aed1-01a411066c94";
		
		final JFrame janela = new JFrame();
		
		JLabel lNomeInvocador = new JLabel("Nome de invocador:");
		lNomeInvocador.setBounds(50, 10, 150, 20);
		
		final JTextField campoNickname = new JTextField();
		campoNickname.setBounds(50, 40, 200, 30);
		
		JButton botao = new JButton("Pesquisar");
		botao.setBounds(260, 40, 100, 30);
		
		final JLabel icone = new JLabel();
		icone.setBounds(50, 80, 50, 50);
		
		final JLabel nickname = new JLabel();
		nickname.setBounds(110, 65, 200, 50);
		
		final JLabel nivel = new JLabel();
		nivel.setBounds(110, 85, 200, 70);
		
		janela.add(nickname);
		janela.add(nivel);
		janela.add(lNomeInvocador);
		janela.add(campoNickname);
		janela.add(icone);
		
		botao.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				// executa a api
				// extrai as informações da API
				// atualiza na tela as informações relacionadas à API
				String nomeInvocador = campoNickname.getText();
				final int fotoId;
				
				//JLabel icone = new JLabel();
				//janela.add(icone);
				
				
				
				
				String url = String.format("https://br1.api.riotgames.com/lol/summoner/v4/summoners/by-name/%s", nomeInvocador);
				Api api = new Api(url);
				
				Map<String, String> cabecalhos = new HashMap<String, String>();
				cabecalhos.put("X-Riot-Token", token);
						
				JSONObject json = null;
				try {
					json = JsonParser.parseToObject(api.executar(cabecalhos));
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				campoNickname.setText("");
				
				fotoId = json.getInt("profileIconId");
				
				nickname.setText(json.getString("name"));
				
				nivel.setText("Level: " + json.getInt("summonerLevel"));
				
				String fotoUrl = String.format("http://ddragon.leagueoflegends.com/cdn/10.13.1/img/profileicon/%d.png", fotoId);
				Image imagem = null;
				try {
					imagem = ImageIO.read(new URL(fotoUrl));
				} catch (MalformedURLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				imagem = imagem.getScaledInstance(50, 50, 100);
				icone.setIcon(new ImageIcon(imagem));
				
				String contaId = json.getString("accountId");
				
				
				String url2 = String.format("https://br1.api.riotgames.com/lol/match/v4/matchlists/by-account/%s?queue=420&endIndex=10", contaId);
				Api api2 = new Api(url2);
				
				JSONObject json2 = null;
				try {
					json2 = JsonParser.parseToObject(api2.executar(cabecalhos));
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				JSONArray partidas = json2.getJSONArray("matches");
				
				JPanel painel = new JPanel();
				painel.setBounds(50, 200, 500, 1000);
				
				janela.add(painel);
				painel.setVisible(false);
				
				
				for(int i=0; i<10 && i<partidas.length(); i++) {
					
					// ---------------  Cada partida aki   --------------------
					
					
					JSONObject partida = partidas.getJSONObject(i);
					String lane = partida.getString("lane");// <------- LANE
					int championId = partida.getInt("champion");
					String nomeCampeao = acharCampeao(championId);
					
					int gameId = partida.getInt("gameId");
					
					String url3 = String.format("https://br1.api.riotgames.com/lol/match/v4/matches/%s", gameId);
					Api api3 = new Api(url3);
					
					JSONObject json3 = null;
					try {
						json3 = JsonParser.parseToObject(api3.executar(cabecalhos));
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					JSONArray participantes = json3.getJSONArray("participants");
					
					int participanteId = 0;
					
					try {
						participanteId = acharParticipante(participantes, championId);
					} catch (MalformedURLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					
					JSONObject participante = participantes.getJSONObject(participanteId); 
					
					JSONObject status = participante.getJSONObject("stats");
					
					if(status.getBoolean("win") == false) {
						JLabel lVitoria = new JLabel("Derrota");
						lVitoria.setBounds(60, 30+65*i, 100, 20);
						painel.add(lVitoria);
					}else {
						JLabel lVitoria = new JLabel("Vitoria");
						lVitoria.setBounds(60, 30+65*i, 100, 20);
						painel.add(lVitoria);
					}
					String frag = String.format("%d/%d/%d", status.getInt("kills"), status.getInt("deaths"), status.getInt("assists"));
					
					JLabel lFrag = new JLabel(frag);
					lFrag.setBounds(60, 0+65*i, 100, 20);
					painel.add(lFrag);
					
					String farm = String.format("Cs: %d", status.getInt("totalMinionsKilled"));
					
					JLabel lFarm = new JLabel(farm);
					lFarm.setBounds(120, 0+65*i, 100, 20);
					painel.add(lFarm);
					
					JLabel lRota = new JLabel(lane);
					lRota.setBounds(120, 30+65*i, 100, 20);
					painel.add(lRota);
					
					
					/*Foto do campeao
					 *farm
					 *vitoria/derrota
					 *frag
					 *lane   v*/ 
					
					
					
					
					
					
					
					
					String iconeCampeao = String.format("http://ddragon.leagueoflegends.com/cdn/10.14.1/img/champion/%s.png", nomeCampeao);
					JLabel icone2 = new JLabel("");
					
					Image imagem2 = null;
					try {
						imagem2 = ImageIO.read(new URL(iconeCampeao));
					} catch (MalformedURLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					imagem2 = imagem2.getScaledInstance(50, 50, 100);
					icone2.setIcon(new ImageIcon(imagem2));
					icone2.setBounds(0, 0+65*i, 50, 50);
					
					painel.add(icone2);
					
					
					// ---------------  Cada partida aki   --------------------
					
					janela.add(painel);
					 
				}
				
				painel.setVisible(true);
				
				painel.setLayout(null);
				janela.add(painel);
				janela.setVisible(false);
				janela.setVisible(true);
			}
		});
		
		janela.add(botao);
		
		janela.setBounds(100, 100, 450, 900);
		janela.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		janela.setTitle("Histórico Rankeadas Solo/Duo");
		janela.setLayout(null);
		janela.setVisible(true);
		
	}
	
	/*private static void imprimirTeste(){
		System.out.println("Testando");
	}*/
	
	private static int acharParticipante(JSONArray participantes, int championId) throws MalformedURLException, IOException {
		int participanteId = 0;
		
		
		
		for(int i=0; i<10; i++) {
			JSONObject participante = participantes.getJSONObject(i);
			
			if(championId == participante.getInt("championId")) {
				participanteId = i;
			}
		}
		
		return participanteId;
	}
	
	private static String acharCampeao(int championId){
		String c[] = new String[876];
		c[266] = "Aatrox";
		c[103] = "Ahri";
		c[84] = "Akali";
		c[12] = "Alistar";
		c[32] = "Amumu";
		c[34] = "Anivia";
		c[1] = "Annie";
		c[523] = "Aphelios";
		c[22] = "Ashe";
		c[136] = "AurelionSol";
		c[268] = "Azir";
		c[432] = "Bard";
		c[53] = "Blitzcrank";
		c[63] = "Brand";
		c[201] = "Braum";
		c[51] = "Caitlyn";
		c[164] = "Camille";
		c[69] = "Cassiopeia";
		c[31] = "Chogath";
		c[42] = "Corki";
		c[122] = "Darius";
		c[131] = "Diana";
		c[119] = "Draven";
		c[36] = "DrMundo";
		c[245] = "Ekko";
		c[60] = "Elise";
		c[28] = "Evelynn";
		c[81] = "Ezreal";
		c[9] = "Fiddlesticks";
		c[114] = "Fiora";
		c[105] = "Fizz";
		c[3] = "Galio";
		c[41] = "Gangplank";
		c[86] = "Garen";
		c[150] = "Gnar";
		c[79] = "Gragas";
		c[104] = "Graves";
		c[120] = "Hecarim";
		c[74] = "Heimerdinger";
		c[420] = "Illaoi";
		c[39] = "Irelia";
		c[427] = "Ivern";
		c[40] = "Janna";
		c[59] = "JarvanIV";
		c[24] = "Jax";
		c[126] = "Jayce";
		c[202] = "Jhin";
		c[222] = "Jinx";
		c[145] = "Kaisa";
		c[429] = "Kalista";
		c[43] = "Karma";
		c[30] = "Karthus";
		c[38] = "Kassadin";
		c[55] = "Katarina";
		c[10] = "Kayle";
		c[141] = "Kayn";
		c[85] = "Kennen";
		c[121] = "Khazix";
		c[203] = "Kindred";
		c[240] = "Kled";
		c[96] = "KogMaw";
		c[7] = "Leblanc";
		c[64] = "LeeSin";
		c[89] = "Leona";
		c[127] = "Lissandra";
		c[236] = "Lucian";
		c[117] = "Lulu";
		c[99] = "Lux";
		c[54] = "Malphite";
		c[90] = "Malzahar";
		c[57] = "Maokai";
		c[11] = "MasterYi";
		c[21] = "MissFortune";
		c[62] = "MonkeyKing";
		c[82] = "Mordekaiser";
		c[25] = "Morgana";
		c[267] = "Nami";
		c[75] = "Nasus";
		c[111] = "Nautilus";
		c[518] = "Neeko";
		c[76] = "Nidalee";
		c[56] = "Nocturne";
		c[20] = "Nunu";
		c[2] = "Olaf";
		c[61] = "Orianna";
		c[516] = "Ornn";
		c[80] = "Pantheon";
		c[78] = "Poppy";
		c[555] = "Pyke";
		c[246] = "Qiyana";
		c[133] = "Quinn";
		c[497] = "Rakan";
		c[33] = "Rammus";
		c[421] = "RekSai";
		c[58] = "Renekton";
		c[107] = "Rengar";
		c[92] = "Riven";
		c[68] = "Rumble";
		c[13] = "Ryze";
		c[113] = "Sejuani";
		c[235] = "Senna";
		c[875] = "Sett";
		c[35] = "Shaco";
		c[98] = "Shen";
		c[102] = "Shyvana";
		c[27] = "Singed";
		c[14] = "Sion";
		c[15] = "Sivir";
		c[72] = "Skarner";
		c[37] = "Sona";
		c[16] = "Soraka";
		c[50] = "Swain";
		c[517] = "Sylas";
		c[134] = "Syndra";
		c[223] = "TahmKench";
		c[163] = "Taliyah";
		c[91] = "Talon";
		c[44] = "Taric";
		c[17] = "Teemo";
		c[412] = "Thresh";
		c[18] = "Tristana";
		c[48] = "Trundle";
		c[23] = "Tryndamere";
		c[4] = "TwistedFate";
		c[29] = "Twitch";
		c[77] = "Udyr";
		c[6] = "Urgot";
		c[110] = "Varus";
		c[67] = "Vayne";
		c[45] = "Veigar";
		c[161] = "Velkoz";
		c[254] = "Vi";
		c[112] = "Viktor";
		c[8] = "Vladimir";
		c[106] = "Volibear";
		c[19] = "Warwick";
		c[498] = "Xayah";
		c[101] = "Xerath";
		c[5] = "XinZhao";
		c[157] = "Yasuo";
		c[83] = "Yorick";
		c[350] = "Yuumi";
		c[154] = "Zac";
		c[238] = "Zed";
		c[115] = "Ziggs";
		c[26] = "Zilean";
		c[142] = "Zoe";
		c[143] = "Zyra";
		
		
		
		return c[championId];
	}

}
