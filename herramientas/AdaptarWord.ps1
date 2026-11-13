param([string]$Plantilla, [string]$Salida)
$ErrorActionPreference='Stop'
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem
Add-Type -AssemblyName System.Drawing
$w='http://schemas.openxmlformats.org/wordprocessingml/2006/main'
$base=Split-Path $PSScriptRoot -Parent
$entrada=[IO.File]::Open($Plantilla,'Open','Read','ReadWrite')
try { $copia=[IO.File]::Create($Salida);try {$entrada.CopyTo($copia)}finally{$copia.Dispose()} } finally {$entrada.Dispose()}
$zip=[IO.Compression.ZipFile]::Open($Salida,'Update')
function LeerXml($ruta) {
    $lector=New-Object IO.StreamReader($zip.GetEntry($ruta).Open())
    try { $xml=New-Object Xml.XmlDocument;$xml.PreserveWhitespace=$true;$xml.LoadXml($lector.ReadToEnd());return ,$xml } finally {$lector.Dispose()}
}
function GuardarXml($ruta,$xml) {
    $viejo=$zip.GetEntry($ruta);if($viejo){$viejo.Delete()}
    $nuevo=$zip.CreateEntry($ruta);$s=$nuevo.Open()
    try {$xml.Save($s)}finally{$s.Dispose()}
}
try {
    $doc=LeerXml 'word/document.xml'
    $ns=New-Object Xml.XmlNamespaceManager($doc.NameTable);$ns.AddNamespace('w',$w)
    $ns.AddNamespace('a','http://schemas.openxmlformats.org/drawingml/2006/main')
    $ns.AddNamespace('wp','http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing')
    $ns.AddNamespace('pic','http://schemas.openxmlformats.org/drawingml/2006/picture')
    $ns.AddNamespace('r','http://schemas.openxmlformats.org/officeDocument/2006/relationships')
    $body=$doc.SelectSingleNode('//w:body',$ns)
    $parrafos=$doc.SelectNodes('//w:body/w:p',$ns)
    $cover=@();for($i=0;$i -le 30;$i++){$cover+=,$parrafos[$i].CloneNode($true)}
    $seccion=$body.SelectSingleNode('w:sectPr',$ns).CloneNode($true)
    $plantillaImagen=$parrafos[0].SelectSingleNode('w:r[w:drawing]',$ns).CloneNode($true)
    $tablas=$doc.SelectNodes('//w:tbl',$ns)
    $tablaBase=$tablas[0].CloneNode($true)
    $tablaCompacta=$tablas[7].CloneNode($true)
    $rels=LeerXml 'word/_rels/document.xml.rels'
    $tipos=LeerXml '[Content_Types].xml'

    function Elemento($nombre) {return ,$doc.CreateElement('w',$nombre,$w)}
    function Atributo($n,$nombre,$valor) {[void]$n.SetAttribute($nombre,$w,[string]$valor)}
    function TextoPlano($s) {return ($s -replace '\*\*','' -replace '`','' -replace '(?<!\*)\*([^*]+)\*','$1')}
    function CambiarTexto($p,$texto) {
        $estilo=$p.SelectSingleNode('w:r/w:rPr',$ns)
        foreach($n in @($p.ChildNodes)){if($n.LocalName -ne 'pPr'){[void]$p.RemoveChild($n)}}
        $r=Elemento 'r';if($estilo){[void]$r.AppendChild($estilo.CloneNode($true))}
        $t=Elemento 't';$t.InnerText=$texto;[void]$r.AppendChild($t);[void]$p.AppendChild($r)
    }
    CambiarTexto $cover[13] 'BODEXA: INVENTARIO, VENTAS Y ALERTAS DE STOCK PARA BODEGAS'
    CambiarTexto $cover[30] '2026 · Grupo 9'
    foreach($n in @($body.ChildNodes)){[void]$body.RemoveChild($n)}
    foreach($p in $cover){[void]$body.AppendChild($p)}

    function Parrafo($texto,$estilo='APA7MAEDICION',$encabezado=$false) {
        $p=Elemento 'p';$pr=Elemento 'pPr';$s=Elemento 'pStyle';Atributo $s 'val' $estilo;[void]$pr.AppendChild($s)
        if($encabezado){
            $num=Elemento 'numPr';$id=Elemento 'numId';Atributo $id 'val' 0;[void]$num.AppendChild($id);[void]$pr.AppendChild($num)
            [void]$pr.AppendChild((Elemento 'keepNext'));[void]$pr.AppendChild((Elemento 'keepLines'))
            if($texto -eq '1. Resumen del proyecto'){[void]$pr.AppendChild((Elemento 'pageBreakBefore'))}
        }
        [void]$p.AppendChild($pr)
        # Conservar énfasis del contenido usando la fuente heredada de la plantilla.
        foreach($trozo in [regex]::Split($texto,'(\*\*.*?\*\*)')) {
            if(!$trozo){continue};$r=Elemento 'r'
            if($trozo.StartsWith('**')){$rp=Elemento 'rPr';[void]$rp.AppendChild((Elemento 'b'));[void]$r.AppendChild($rp)}
            $t=Elemento 't';[void]$t.SetAttribute('space','http://www.w3.org/XML/1998/namespace','preserve');$t.InnerText=TextoPlano $trozo
            [void]$r.AppendChild($t);[void]$p.AppendChild($r)
        }
        return ,$p
    }
    function AgregarTabla($filas) {
        $cantidad=$filas[0].Count
        $modelo=if($cantidad -ge 5){$tablaCompacta}else{$tablaBase}
        $tabla=$modelo.CloneNode($true)
        foreach($n in @($tabla.ChildNodes)){if($n.LocalName -ne 'tblPr'){[void]$tabla.RemoveChild($n)}}
        $ancho=9360
        $pesos=switch($cantidad){2 {@(1,2)} 3 {@(1,2,2)} 4 {@(1,1.4,2,1.5)} 5 {@(.85,1.2,2.55,.8,1.7)} 6 {@(.85,1.3,1.6,1.55,1.5,.9)} default {@(1)*$cantidad}}
        $suma=($pesos|Measure-Object -Sum).Sum
        $grid=Elemento 'tblGrid';foreach($peso in $pesos){$c=Elemento 'gridCol';Atributo $c 'w' ([int]($ancho*$peso/$suma));[void]$grid.AppendChild($c)};[void]$tabla.AppendChild($grid)
        for($i=0;$i -lt $filas.Count;$i++) {
            $tr=Elemento 'tr';$trPr=Elemento 'trPr';[void]$trPr.AppendChild((Elemento 'cantSplit'));if($i -eq 0){[void]$trPr.AppendChild((Elemento 'tblHeader'))};[void]$tr.AppendChild($trPr)
            $filaModelo=if($i -eq 0){1}else{2}
            for($j=0;$j -lt $cantidad;$j++) {
                $celda=$modelo.SelectSingleNode("w:tr[$filaModelo]/w:tc[1]",$ns).CloneNode($true)
                [void]$celda.SelectSingleNode('w:tcPr/w:tcW',$ns).SetAttribute('w',$w,[string][int]($ancho*$pesos[$j]/$suma))
                $p=$celda.SelectSingleNode('w:p',$ns)
                CambiarTexto $p (TextoPlano $filas[$i][$j].Trim())
                if($i -eq 0){
                    $pr=$p.SelectSingleNode('w:pPr',$ns)
                    if(!$pr){$pr=Elemento 'pPr';[void]$p.PrependChild($pr)}
                    [void]$pr.AppendChild((Elemento 'keepNext'))
                }
                foreach($otro in @($celda.SelectNodes('w:p[position()>1]',$ns))){[void]$celda.RemoveChild($otro)}
                [void]$tr.AppendChild($celda)
            }
            [void]$tabla.AppendChild($tr)
        }
        [void]$body.AppendChild($tabla)
        [void]$body.AppendChild((Parrafo '' 'Normal'))
    }
    $script:img=100
    function AgregarImagen($ruta,$descripcion) {
        $script:img++
        $rid='rIdBodexa'+$script:img;$archivo='bodexa_'+$script:img+'.png'
        $entry=$zip.CreateEntry('word/media/'+$archivo);$flujo=$entry.Open();$origen=[IO.File]::OpenRead($ruta)
        try{$origen.CopyTo($flujo)}finally{$origen.Dispose();$flujo.Dispose()}
        $rel=$rels.CreateElement('Relationship',$rels.DocumentElement.NamespaceURI)
        $rel.SetAttribute('Id',$rid);$rel.SetAttribute('Type','http://schemas.openxmlformats.org/officeDocument/2006/relationships/image');$rel.SetAttribute('Target','media/'+$archivo);[void]$rels.DocumentElement.AppendChild($rel)
        $imagen=[Drawing.Image]::FromFile($ruta)
        try{$cx=5943600;$cy=[long]($cx*$imagen.Height/$imagen.Width)}finally{$imagen.Dispose()}
        $p=Parrafo '' 'Normal';$pr=$p.SelectSingleNode('w:pPr',$ns);$jc=Elemento 'jc';Atributo $jc 'val' 'center';[void]$pr.AppendChild($jc)
        [void]$pr.AppendChild((Elemento 'keepNext'))
        $run=$plantillaImagen.CloneNode($true)
        $extent=$run.SelectSingleNode('.//wp:extent',$ns);$extent.SetAttribute('cx',[string]$cx);$extent.SetAttribute('cy',[string]$cy)
        $ext=$run.SelectSingleNode('.//a:xfrm/a:ext',$ns);$ext.SetAttribute('cx',[string]$cx);$ext.SetAttribute('cy',[string]$cy)
        $dp=$run.SelectSingleNode('.//wp:docPr',$ns);$dp.SetAttribute('id',[string]$script:img);$dp.SetAttribute('name',$archivo);$dp.SetAttribute('descr',$descripcion)
        $pic=$run.SelectSingleNode('.//pic:cNvPr',$ns);$pic.SetAttribute('name',$archivo);$pic.SetAttribute('descr',$descripcion)
        [void]$run.SelectSingleNode('.//a:blip',$ns).SetAttribute('embed',$ns.LookupNamespace('r'),$rid)
        [void]$p.AppendChild($run);[void]$body.AppendChild($p)
        $pie=Parrafo $descripcion 'Normal';$rp=Elemento 'rPr';$sz=Elemento 'sz';Atributo $sz 'val' 18;[void]$rp.AppendChild($sz)
        # Times New Roman 9 pt, como las leyendas de figura de la plantilla.
        [void]$pie.SelectSingleNode('w:r',$ns).PrependChild($rp)
        [void]$body.AppendChild($pie)
    }
    $md=[IO.File]::ReadAllText((Join-Path $base 'docs/INFORME.md'))
    $md=$md.Replace('{{PRUEBAS}}',[IO.File]::ReadAllText((Join-Path $base 'docs/PRUEBAS.md'))).Replace('{{SQL}}',[IO.File]::ReadAllText((Join-Path $base 'docs/evidencias/pruebas_sql.txt')))
    $md=$md.Substring($md.IndexOf('## 1. Resumen'))
    $lineas=$md -split '\r?\n'
    for($i=0;$i -lt $lineas.Count;$i++) {
        $l=$lineas[$i].Trim();if(!$l){continue}
        if($l.StartsWith('|')) {
            $filas=New-Object Collections.Generic.List[object]
            while($i -lt $lineas.Count -and $lineas[$i].Trim().StartsWith('|')) {
                $fila=$lineas[$i].Trim()
                if($fila -notmatch '^[| :\-]+$'){$filas.Add(($fila.Trim('|') -split '\|'))};$i++
            };$i--;AgregarTabla $filas;continue
        }
        if($l -match '^!\[(.*?)\]\((.*?)\)$'){AgregarImagen (Join-Path (Join-Path $base 'docs') $Matches[2]) $Matches[1];continue}
        if($l -match '^(#+)\s+(.*)$') {
            $nivel=$Matches[1].Length;$texto=$Matches[2];$estilo=if($nivel -le 2){'Ttulo1'}else{'Ttulo2'}
            [void]$body.AppendChild((Parrafo $texto $estilo $true));continue
        }
        [void]$body.AppendChild((Parrafo $l))
    }
    [void]$body.AppendChild($seccion)
    if(!$tipos.DocumentElement.SelectSingleNode("*[local-name()='Default' and @Extension='png']")) {
        $tipo=$tipos.CreateElement('Default',$tipos.DocumentElement.NamespaceURI);$tipo.SetAttribute('Extension','png');$tipo.SetAttribute('ContentType','image/png');[void]$tipos.DocumentElement.AppendChild($tipo)
    }
    GuardarXml 'word/document.xml' $doc
    GuardarXml 'word/_rels/document.xml.rels' $rels
    GuardarXml '[Content_Types].xml' $tipos
    foreach($nombreCabecera in @($zip.Entries | Where-Object {$_.FullName -match '^word/header\d+\.xml$'} | ForEach-Object {$_.FullName})) {
        $cabecera=LeerXml $nombreCabecera
        $hn=New-Object Xml.XmlNamespaceManager($cabecera.NameTable);$hn.AddNamespace('w',$w)
        foreach($p in $cabecera.SelectNodes('//w:p',$hn)) {
            $textos=$p.SelectNodes('.//w:t',$hn)
            $contenido=($textos|ForEach-Object{$_.InnerText}) -join ''
            if($contenido -match 'SISTEMA DE GESTI.N DE INVENTARIO') {
                $textos[0].InnerText='< BODEXA: INVENTARIO, VENTAS Y ALERTAS DE STOCK PARA BODEGAS >'
                for($j=1;$j -lt $textos.Count;$j++){$textos[$j].InnerText=''}
            }
        }
        GuardarXml $nombreCabecera $cabecera
    }
    Write-Output "Word creado: $Salida"
    Write-Output ('Tablas: '+$body.SelectNodes('w:tbl',$ns).Count+'; imágenes Bodexa: '+($script:img-100))
} finally {$zip.Dispose()}
